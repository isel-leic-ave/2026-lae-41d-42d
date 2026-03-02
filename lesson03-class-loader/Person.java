class Student implements Person {
    public void print() {
        System.out.println("I am Student");
    }
}

class Teacher implements Person {
    public void print() {
        System.out.println("I am Teacher");
    }
}

interface Person {}
