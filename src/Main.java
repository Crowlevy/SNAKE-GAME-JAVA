import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.sound.sampled.*;
import javax.swing.*;

public class Main extends JPanel implements KeyListener, ActionListener {
    private final int LARGURA_TELA = 800;
    private final int ALTURA_TELA = 600;
    private final int TAMANHO_BLOCO = 25;
    private int NUM_BLOCKS_X = LARGURA_TELA / TAMANHO_BLOCO;
    private int NUM_BLOCKS_Y = ALTURA_TELA / TAMANHO_BLOCO;
    private ArrayList<Point> cobrinha;
    private Point comida;
    private char direcao;
    private Timer timer;
    private int score;
    private ImageIcon overImage;
    private ImageIcon startImage;
    private Clip som;
    private int nivelDificuldade;
    private boolean powerUpAtivo;
    private Point powerUp;

    public Main() {
        cobrinha = new ArrayList<>();
        cobrinha.add(new Point(NUM_BLOCKS_X / 2, NUM_BLOCKS_Y / 2));
        comida = new Point();
        direcao = ' ';
        timer = new Timer(100, this);
        score = 0;
        nivelDificuldade = 1;
        powerUpAtivo = false;

        overImage = new ImageIcon(getClass().getResource("gameoverREAL.jpg"));
        startImage = new ImageIcon(getClass().getResource("start.jpg"));

        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getResource("mano.wav"));
            som = AudioSystem.getClip();
            som.open(audioInputStream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        addKeyListener(this);
        setFocusable(true);
        JOptionPane.showMessageDialog(this, "", "COMEÇOU EBAAA", JOptionPane.INFORMATION_MESSAGE, startImage);
        timer.start();
        gerarComida();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        desenhar(g);
    }

    private void desenhar(Graphics g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, LARGURA_TELA, ALTURA_TELA);
        g.setColor(Color.CYAN);
        for (Point point : cobrinha) {
            g.fillRect(point.x * TAMANHO_BLOCO, point.y * TAMANHO_BLOCO, TAMANHO_BLOCO, TAMANHO_BLOCO);
        }
        g.setColor(Color.RED);
        g.fillRect(comida.x * TAMANHO_BLOCO, comida.y * TAMANHO_BLOCO, TAMANHO_BLOCO, TAMANHO_BLOCO);

        if (powerUpAtivo) {
            g.setColor(Color.YELLOW);
            g.fillRect(powerUp.x * TAMANHO_BLOCO, powerUp.y * TAMANHO_BLOCO, TAMANHO_BLOCO, TAMANHO_BLOCO);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("SCORE EBA: " + score, 20, 30);
        g.drawString("NÍVEL DE DIFICULDADE: " + nivelDificuldade, 20, 60);
    }
    private void ajustarVelocidade() {
        int intervaloInicial = 100;

        int intervalo = intervaloInicial - (nivelDificuldade * 10);

        if (intervalo < 50) {
            intervalo = 50;
        }

        timer.setDelay(intervalo);
    }
    private void aumentarDificuldade(){
        for(int i = 0;i<nivelDificuldade;i++){
            NUM_BLOCKS_Y += 1;
            NUM_BLOCKS_X +=1;
        }
        ajustarVelocidade();
    }
    private void gerarComida() {
        Random random = new Random();
        comida.setLocation(random.nextInt(NUM_BLOCKS_X), random.nextInt(NUM_BLOCKS_Y));
    }

    private void gerarPowerUp() {
        Random random = new Random();
        powerUp = new Point(random.nextInt(NUM_BLOCKS_X), random.nextInt(NUM_BLOCKS_Y));
        powerUpAtivo = true;
        if (powerUpAtivo == true){
            Point ultimoBloco = cobrinha.get(cobrinha.size() - 1);
            cobrinha.add(new Point(ultimoBloco.x+2, ultimoBloco.y+2));
            score+=100;
        }
    }

    private void reiniciarPowerUp() {
        powerUpAtivo = false;
        powerUp = null;
    }

    private void aumentarVelocidade() {
        timer.setDelay(timer.getDelay() - 10);
    }

    private void aumentarTamanho() {
        Point ultimoBloco = cobrinha.get(cobrinha.size() - 1);
        cobrinha.add(new Point(ultimoBloco.x, ultimoBloco.y));
    }

    private void movimentacao() {
        Point cabeca = new Point(cobrinha.get(0));
        switch (direcao) {
            case 'w':
                cabeca.y--;
                break;
            case 's':
                cabeca.y++;
                break;
            case 'a':
                cabeca.x--;
                break;
            case 'd':
                cabeca.x++;
                break;
        }
        if (cabeca.equals(comida)) {
            cobrinha.add(0, cabeca);
            gerarComida();
            score += 10;
            if (score % 50 == 0) {
                nivelDificuldade++;
            }
            if (score % 100 == 0) {
                gerarPowerUp();
            }
        } else {
            cobrinha.add(0, cabeca);
            cobrinha.remove(cobrinha.size() - 1);
        }

        if (powerUpAtivo && cobrinha.get(0).equals(powerUp)) {
            aumentarVelocidade();
            reiniciarPowerUp();
        }
    }
    private int pontuacaoAnterior = 0;

    private void reiniciarJogo() {
        cobrinha.clear();
        cobrinha.add(new Point(NUM_BLOCKS_X / 2, NUM_BLOCKS_Y / 2));
        score = 0;
        gerarComida();
        direcao = ' ';
        timer.restart();
        nivelDificuldade = 1;
    }
    private void continuarJogo(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                int resposta = JOptionPane.showConfirmDialog(this, "Deseja reiniciar o jogo?", "Confirmação", JOptionPane.YES_NO_OPTION);
                if (resposta == JOptionPane.YES_OPTION) {
                    if (score > pontuacaoAnterior) {
                        pontuacaoAnterior = score;
                    }
                    reiniciarJogo();
                }
                break;
        }
    }


    private boolean colidir() {
        Point cabeca = cobrinha.get(0);
        if (cabeca.x < 0 || cabeca.x >= NUM_BLOCKS_X || cabeca.y < 0 || cabeca.y >= NUM_BLOCKS_Y) {
            return true;
        }
        for (int i = 1; i < cobrinha.size(); i++) {
            if (cabeca.equals(cobrinha.get(i))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        movimentacao();
        if (colidir()) {
            timer.stop();
            if (som != null) {
                som.stop();
                som.setFramePosition(0);
                som.start();
            }
            JOptionPane.showMessageDialog(this, "PONTUAÇÃO: " + score, "PERDEU LADRÃO", JOptionPane.INFORMATION_MESSAGE, overImage);
            reiniciarJogo();
        }
        repaint();
    }


    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W:
                if (direcao != 's') {
                    direcao = 'w';
                }
                break;
            case KeyEvent.VK_S:
                if (direcao != 'w') {
                    direcao = 's';
                }
                break;
            case KeyEvent.VK_A:
                if (direcao != 'd') {
                    direcao = 'a';
                }
                break;
            case KeyEvent.VK_D:
                if (direcao != 'a') {
                    direcao = 'd';
                }
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("JOGUINHO DA COBRINHA");
        Main jogo = new Main();
        frame.add(jogo);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setVisible(true);
        jogo.requestFocusInWindow();
    }
}
