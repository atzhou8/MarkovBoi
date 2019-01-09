package threads;

import bot.Bot;

public class SaveThread extends Thread implements Runnable {

    @Override
    public void run() {
        System.out.println("Saving");
        Bot.save();
        System.out.println("Save completed");
    }

}
