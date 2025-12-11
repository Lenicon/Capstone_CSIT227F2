import javazoom.jl.player.Player;
import java.io.FileInputStream;

public class BGM_Pomodoro {
    private Player player;
    private boolean isPlaying = false;
    private boolean loop = false;

    public void play(String filePath, boolean loopMusic) {
        if (filePath == "") return;

        loop = loopMusic;
        if (isPlaying) return;

        isPlaying = true;
        new Thread(() -> {
            do {
                try {
                    FileInputStream fis = new FileInputStream(filePath);
                    player = new Player(fis);
                    player.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } while (loop);
            isPlaying = false;
        }).start();
    }

    public void stop() {
        loop = false;
        if (player != null) {
            player.close();
            isPlaying = false;
        }
    }
}
