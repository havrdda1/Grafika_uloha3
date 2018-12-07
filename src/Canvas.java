import imagedata.Image;
import imagedata.ImageAWT;
import imagedata.Presenter;
import imagedata.PresenterAWT;
import org.jetbrains.annotations.NotNull;
import rasterops.LineRenderer;
import rasterops.LineRendererDDA;
import solidops.WireframeRenderer;
import solidsdata.*;
import transforms.*;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * trida pro kresleni na platno: zobrazeni pixelu
 *
 * @author PGRF FIM UHK
 * @version 2017
 */

public class Canvas {

    private final JFrame frame;
    private final JPanel panel;
    private final BufferedImage img;

    private Camera camera;

    private final ArrayList<Solid> solidBuffer;
    private final ArrayList<Color> colorBuffer;
    private ArrayList<Point3D> meshBuffer;

    private Image image;
    private final @NotNull Presenter<Color, Graphics> presenter;

    private final @NotNull LineRenderer<Color> lineRenderer;
    private int startC, startR;

    public Canvas(final int width, final int height) {
        frame = new JFrame();

        frame.setLayout(new BorderLayout());
        frame.setTitle("UHK FIM PGRF : " + this.getClass().getName());
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        solidBuffer = new ArrayList<>();
        colorBuffer = new ArrayList<>();
//*
        image = new ImageAWT<>(
                img,
                //Function<PixelType,Integer>, kde PixelType = Color
                (Color c) -> c.getRGB(),
                //Function<Integer,PixelType>, kde PixelType = Color
                (Integer i) -> new Color(i)
        );
        presenter = new PresenterAWT<>();
/*/
		rasterImage = RasterImageImmu.cleared(
				width, height, new Color(255,0,0));
		presenter = new ImagePresenterUniversal<>(color -> color.getRGB());
//*/
        lineRenderer = new LineRendererDDA<>();

        camera = new Camera()
                .withPosition(new Vec3D(3,3,2))
                .withAzimuth(Math.PI*1.25)
                .withZenith(-Math.atan(1.0 / 5.0));

        //Nastavení zásobníku objektu a barev

        solidBuffer.add(new AxisX());
        solidBuffer.add(new AxisY());
        solidBuffer.add(new AxisZ());
        solidBuffer.add(new CustomSolid());
        solidBuffer.add(new PolygonMesh(10));

        colorBuffer.add(new Color(1.0f, 0, 0));
        colorBuffer.add(new Color(0, 1.0f, 0));
        colorBuffer.add(new Color(0, 0, 1.0f));
        colorBuffer.add(new Color(1.0f, 0, 1));
        colorBuffer.add(new Color(1.0f, 1, 0));

        meshBuffer = new ArrayList<>(16);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                int z = 1;
                if (i > 0 && i < 3 && j > 1 && j < 3)
                    z = 2;
                meshBuffer.add(new Point3D(i, j, z));
                if (16 < meshBuffer.size())
                    System.out.println("Mesh má více bodů než 16");
            }
        }

        panel = new JPanel() {
            private static final long serialVersionUID = 1L;

            @Override
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
                present(g);
            }
        };
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_W:
                        camera = camera.forward(0.1);
                        System.out.println("Dopředu");
                        break;

                    case KeyEvent.VK_S:
                        camera = camera.backward(0.1);
                        System.out.println("Dozadu");
                        break;

                    case KeyEvent.VK_A:
                        camera = camera.left(0.1);
                        System.out.println("Doleva");
                        break;

                    case KeyEvent.VK_D:
                        camera = camera.right(0.1);
                        System.out.println("Doprava");
                        break;

                }
                draw();
                panel.repaint();
            }
        });
        panel.setPreferredSize(new Dimension(width, height));
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startC = e.getX();
                startR = e.getY();
            }
        });
        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                final int endC = e.getX();
                final int endR = e.getY();

                final double x1 =
                        2 * (startC + 0.5) / image.getWidth() - 1;
                final double y1 =
                        -(2 * (startR + 0.5) / image.getHeight() - 1);
                final double x2 =
                        2 * (endC + 0.5) / image.getWidth() - 1;
                final double y2 =
                        -(2 * (endR + 0.5) / image.getHeight() - 1);

                camera = moveCam(camera, new Vec2D(x2-x1, y2-y1));
                draw();
                panel.repaint();

                startC = endC;
                startR = endR;
            }
        });

        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    private Camera moveCam(Camera cam, Vec2D vec) {

        double x = cam.getAzimuth() * (1 - vec.getX() * 0.5);
        //float y = (float)(camera.getZenith() * (1 - vec.getY() * 5));
        double y = cam.getZenith() + vec.getY();

        cam = cam.withAzimuth(x);
        cam = cam.withZenith(y);
        return cam;
    }

    public void clear() {
        image = image.cleared(new Color(0x2f, 0x2f, 0x2f));
    }

    public void present(final Graphics graphics) {
        presenter.present(image, graphics);

        graphics.setColor(Color.WHITE);
        graphics.drawString("A,W,S,D -> pohyb", 10, 30);
        graphics.drawString("Mouse_BTN1 -> otáčení kamerou ", 10, 45);
    }
    public void help() {

    }

    public void draw() {
        clear();


        for (int i = 0; i < solidBuffer.size(); i++) {
            image =
                    new WireframeRenderer<Color>(lineRenderer)
                            .render(image, solidBuffer.get(i),
                                    camera.getViewMatrix()
                                          .mul(new Mat4PerspRH(
                                                  Math.PI / 2.5,
                                                  image.getHeight() / (double) image.getWidth(),
                                                  0.1, 1000
                                          )),
                                    colorBuffer.get(i));
            help();
        }
    }

    public void start() {
        draw();
        panel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Canvas(800, 600)::start);
    }

}