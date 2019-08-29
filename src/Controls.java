import javax.swing.*;

public abstract class Controls {
    static void load(ControlHandler handler, Client client){
        handler.addControl(new MoveUp(client));
        handler.addControl(new MoveDown(client));
        handler.addControl(new MoveLeft(client));
        handler.addControl(new MoveRight(client));
        handler.addControl(new CloseFrame(client.getFrame()));
    }

    static class MoveUp extends Control {
        Client client;

        MoveUp(Client client) {
            this.client = client;
            setEvent(new Control.Factory(87).getKeyEvent());
        }

        @Override
        public void function() {
            client.sendUDP(Movement.UP);
        }
    }

    static class MoveDown extends Control {
        Client client;

        MoveDown(Client client) {
            this.client = client;
            setEvent(new Control.Factory(83).getKeyEvent());
        }

        @Override
        public void function() {
            client.sendUDP(Movement.DOWN);
        }
    }

    static class MoveLeft extends Control {
        Client client;

        MoveLeft(Client client) {
            this.client = client;
            setEvent(new Control.Factory(65).getKeyEvent());
        }

        @Override
        public void function() {
            client.sendUDP(Movement.LEFT);
        }
    }

    static class MoveRight extends Control {
        Client client;

        MoveRight(Client client) {
            this.client = client;
            setEvent(new Control.Factory(68).getKeyEvent());
        }

        @Override
        public void function() {
            client.sendUDP(Movement.RIGHT);
        }
    }

    static class CloseFrame extends Control {
        JFrame frame;

        CloseFrame(JFrame frame) {
            this.frame = frame;
            setEvent(new Control.Factory(27).getKeyEvent());
        }

        @Override
        public void function() {
            frame.dispose();
        }
    }
}
