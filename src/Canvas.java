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
 * class for rasterizing of canvas
 *
 * @author Havrda Daniel
 * @version 2018
 * author of transforms library: Jan Vanek
 */

public class Canvas {

    private final JPanel panel;
    private final ArrayList<Solid> solidBuffer;
    private final ArrayList<Color> colorBuffer;
    private final Mat4Identity matPersp;
    private final Mat4Identity matOrtho;
    private final @NotNull Presenter<Color, Graphics> presenter;
    private final @NotNull LineRenderer<Color> lineRenderer;
    private final double STEP_ROTATION = 0.1;
    private final double STEP_MOVE = 0.1;
    private Camera camera;
    private Mat4 matMultiply;
    private Mat4 matView;
    private Image image;
    private boolean isPersp;
    private int startC, startR;

    private Canvas(final int width, final int height) {

        JFrame frame = new JFrame();

        isPersp = true;
        frame.setLayout(new BorderLayout());
        frame.setTitle("UHK FIM PGRF 1 " + this.getClass()
                                               .getName());
        frame.setResizable(false);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        solidBuffer = new ArrayList<>();
        colorBuffer = new ArrayList<>();

        image = new ImageAWT<>(
                img,

                Color::getRGB,

                Color::new
        );
        matPersp = new Mat4PerspRH(
                Math.PI / 2.5,
                image.getHeight() / (double) image.getWidth(),
                0.1, 1000
        );
        matOrtho = new Mat4OrthoRH(image.getWidth() / 100, image.getHeight() / 100, 0.1, 1000);
        matView = matPersp;
        matMultiply = matPersp;
        presenter = new PresenterAWT<>();
        lineRenderer = new LineRendererDDA<>();

        camera = new Camera()
                .withPosition(new Vec3D(3, 3, 2))
                .withAzimuth(Math.PI * 1.25)
                .withZenith(-Math.atan(1.0 / 5.0));

        solidBuffer.add(new AxisX());
        solidBuffer.add(new AxisY());
        solidBuffer.add(new AxisZ());
        solidBuffer.add(new CustomSolid());
        solidBuffer.add(new Tetrahedron());
        solidBuffer.add(new PolygonMesh(20));

        colorBuffer.add(Color.RED);
        colorBuffer.add(Color.GREEN);
        colorBuffer.add(Color.BLUE);
        colorBuffer.add(Color.YELLOW);
        colorBuffer.add(Color.MAGENTA);
        colorBuffer.add(Color.CYAN);

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
                        camera = camera.forward(0.2);
                        break;

                    case KeyEvent.VK_S:
                        camera = camera.backward(0.2);
                        break;

                    case KeyEvent.VK_A:
                        camera = camera.left(0.2);
                        break;

                    case KeyEvent.VK_D:
                        camera = camera.right(0.2);
                        break;

                    case KeyEvent.VK_P:
                        if (isPersp) {
                            matMultiply = matPersp;
                            matView = matPersp;
                        } else {
                            matMultiply = matOrtho;
                            matView = matOrtho;
                        }
                        isPersp = !isPersp;
                        break;

                    case KeyEvent.VK_N:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4Scale(0.75, 0.75, 0.75)));
                        break;
                    case KeyEvent.VK_M:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4Scale(1.25, 1.25, 1.25)));
                        break;

                    case KeyEvent.VK_LEFT:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4Transl(-STEP_MOVE, 0, 0)));
                        break;
                    case KeyEvent.VK_RIGHT:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4Transl(STEP_MOVE, 0, 0)));
                        break;
                    case KeyEvent.VK_UP:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4Transl(0, STEP_MOVE, 0)));
                        break;
                    case KeyEvent.VK_DOWN:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4Transl(0, -STEP_MOVE, 0)));
                        break;
                    case KeyEvent.VK_K:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4Transl(0, 0, STEP_MOVE)));
                        break;
                    case KeyEvent.VK_L:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4Transl(0, 0, -STEP_MOVE)));
                        break;
                    case KeyEvent.VK_NUMPAD4:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4RotX(-STEP_ROTATION)));
                        break;
                    case KeyEvent.VK_NUMPAD6:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4RotX(STEP_ROTATION)));
                        break;
                    case KeyEvent.VK_NUMPAD8:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4RotY(STEP_ROTATION)));
                        break;
                    case KeyEvent.VK_NUMPAD2:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4RotY(-STEP_ROTATION)));
                        break;
                    case KeyEvent.VK_NUMPAD7:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4RotZ(STEP_ROTATION)));
                        break;
                    case KeyEvent.VK_NUMPAD1:
                        matMultiply = new Mat4(matMultiply.mul(new Mat4RotZ(-STEP_ROTATION)));
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

                camera = rotateCam(camera, new Vec2D(x2 - x1, y2 - y1));
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Canvas(800, 600)::start);
    }

    private Camera rotateCam(Camera cam, Vec2D vec) {

        double x = cam.getAzimuth() * (1 - vec.getX() * 0.5);
        double y = cam.getZenith() + vec.getY();

        cam = cam.withAzimuth(x);
        cam = cam.withZenith(y);
        return cam;
    }

    private void clear() {
        image = image.cleared(new Color(0x2f, 0x2f, 0x2f));
    }

    private void present(final Graphics graphics) {
        presenter.present(image, graphics);

        graphics.setColor(Color.WHITE);
        graphics.drawString("Move A,W,S,D, move solids using arrows", 5, 30);
        graphics.drawString("Rotate camera using left button ", 5, 45);
        graphics.drawString("Change of perspective P", 5, 60);
        graphics.drawString("Scale change: M,N", 5, 75);
        graphics.drawString("Rotate objects using numpad: 4,6,8,2,7,1", 5, 90);

    }

    private void help() {

    }

    private void draw() {
        clear();

        for (int i = 0; i < 3; i++) {
            image =
                    new WireframeRenderer<>(lineRenderer)
                            .render(image, solidBuffer.get(i),
                                    camera.getViewMatrix()
                                          .mul(matView),
                                    colorBuffer.get(i));
        }


        for (int i = 3; i < solidBuffer.size(); i++) {
            image =
                    new WireframeRenderer<>(lineRenderer)
                            .render(image, solidBuffer.get(i),
                                    camera.getViewMatrix()
                                          .mul(matMultiply),
                                    colorBuffer.get(i));
        }
    }

    private void start() {
        draw();
        panel.repaint();
    }

}