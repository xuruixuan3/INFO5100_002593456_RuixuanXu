// Base abstract class
abstract class Shape {

    // static field (shared by all shapes)
    public static String color = "Blue";

    // abstract methods (abstraction)
    public abstract double calculateArea();
    public abstract double calculatePerimeter();

    // concrete method
    public void printColor() {
        System.out.println("Color: " + color);
    }
}

// Triangle class
class Triangle extends Shape {
    private double base;
    private double height;
    private double sideA;
    private double sideB;
    private double sideC;

    public Triangle(double base, double height, double a, double b, double c) {
        this.base = base;
        this.height = height;
        this.sideA = a;
        this.sideB = b;
        this.sideC = c;
    }

    @Override
    public double calculateArea() {
        return 0.5 * base * height;
    }

    @Override
    public double calculatePerimeter() {
        return sideA + sideB + sideC;
    }
}

// Rectangle class
class Rectangle extends Shape {
    protected double length;
    protected double width;

    public Rectangle(double length, double width) {
        this.length = length;
        this.width = width;
    }

    @Override
    public double calculateArea() {
        return length * width;
    }

    @Override
    public double calculatePerimeter() {
        return 2 * (length + width);
    }
}

// Square class (special type of Rectangle)
class Square extends Shape {
    private double side;

    public Square(double side) {
        this.side = side;
    }

    @Override
    public double calculateArea() {
        return side * side;
    }

    @Override
    public double calculatePerimeter() {
        return 4 * side;
    }
}

// Circle class
class Circle extends Shape {
    private double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }

    @Override
    public double calculatePerimeter() {
        return 2 * Math.PI * radius;
    }
}

// Main class
public class ShapeDemo {

    public static void main(String[] args) {

        // Polymorphism: Shape reference holding different objects
        Shape[] shapes = {
                new Triangle(5, 4, 5, 4, 3),
                new Rectangle(6, 4),
                new Square(5),
                new Circle(3)};

        System.out.println("=== SHAPE DEMO ===");
        System.out.println("Static Color (shared): " + Shape.color);
        System.out.println();

        for (Shape shape : shapes) {
            System.out.println("Shape: " + shape.getClass().getSimpleName());
            shape.printColor();
            System.out.println("Area: " + shape.calculateArea());
            System.out.println("Perimeter: " + shape.calculatePerimeter());
            System.out.println();
        }
    }
}
