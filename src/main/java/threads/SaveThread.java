package threads;

import bot.Bot;

public class SaveThread extends Thread implements Runnable {

    @Override
    public void run() {
        Bot.save();
    }

}
