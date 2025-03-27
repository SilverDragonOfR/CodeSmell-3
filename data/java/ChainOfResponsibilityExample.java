import java.util.ArrayList;
import java.util.List;

// Abstract Handler
interface AbstractHandler {
    public void setNextHandler(AbstractHandler handler);
    public void handleRequest(int request);
}

// Concrete Handler
class ConcreteHandler1 implements AbstractHandler {
    private AbstractHandler nextHandler;

    @Override
    public void setNextHandler(AbstractHandler handler) {
        this.nextHandler = handler;
    }
    @Override
    public void handleRequest(int request) {
        if (request == 1) {
            System.out.println("Request handled by ConcreteHandler1");
        } else {
            nextHandler.handleRequest(request);
        }
    }
}
class ConcreteHandler2 implements AbstractHandler {
    private AbstractHandler nextHandler;

    @Override
    public void setNextHandler(AbstractHandler handler) {
        this.nextHandler = handler;
    }

    @Override
    public void handleRequest(int request) {
        if (request == 2) {
            System.out.println("Request handled by ConcreteHandler2");
        } else {
            nextHandler.handleRequest(request);
        }
    }
}


public class ChainOfResponsibilityExample {
    public static void main(String[] args) {
        AbstractHandler handler1 = new ConcreteHandler1();
        AbstractHandler handler2 = new ConcreteHandler2();

        handler1.setNextHandler(handler2);
        handler1.handleRequest(1);
        handler1.handleRequest(2);
    }
}
