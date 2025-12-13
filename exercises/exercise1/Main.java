import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

abstract class Student {
    private final String name;
    private final int[] quizScores; // 15 quiz scores

    public Student(String name, int[] quizScores) {
        if (quizScores == null || quizScores.length != 15) {
            throw new IllegalArgumentException("quizScores must contain exactly 15 scores.");
        }
        this.name = name;
        this.quizScores = quizScores;
    }

    public String getName() {
        return name;
    }

    public int[] getQuizScores() {
        return quizScores;
    }

    public double getAverageQuizScore() {
        int sum = 0;
        for (int score : quizScores) sum += score;
        return sum / 15.0;
    }
}

class PartTimeStudent extends Student {
    public PartTimeStudent(String name, int[] quizScores) {
        super(name, quizScores);
    }
}

class FullTimeStudent extends Student {
    private final int examScore1;
    private final int examScore2;

    public FullTimeStudent(String name, int[] quizScores, int examScore1, int examScore2) {
        super(name, quizScores);
        this.examScore1 = examScore1;
        this.examScore2 = examScore2;
    }

    public int getExamScore1() {
        return examScore1;
    }

    public int getExamScore2() {
        return examScore2;
    }
}

class Session {
    private final List<Student> students; // holds 20 students

    public Session() {
        this.students = new ArrayList<>();
    }

    // Add a student to the session (up to 20)
    public void addStudent(Student student) {
        if (students.size() >= 20) {
            throw new IllegalStateException("Session can hold only 20 students.");
        }
        students.add(student);
    }

    // 1) Calculate average quiz scores per student for the whole class
    public void printAverageQuizScoresPerStudent() {
        System.out.println("\n--- Average Quiz Score Per Student ---");
        for (Student s : students) {
            System.out.printf("%s: %.2f%n", s.getName(), s.getAverageQuizScore());
        }
    }

    // 2) Print the list of quiz scores in ascending order for one session
    // Interpreted as: all quiz scores of all students in this session, sorted ascending.
    public void printAllQuizScoresAscending() {
        List<Integer> allScores = new ArrayList<>(students.size() * 15);
        for (Student s : students) {
            for (int score : s.getQuizScores()) {
                allScores.add(score);
            }
        }
        Collections.sort(allScores);

        System.out.println("\n--- All Quiz Scores (Session) Ascending ---");
        System.out.println(allScores);
    }

    // 3) Print names of part-time students
    public void printPartTimeStudentNames() {
        System.out.println("\n--- Part-Time Student Names ---");
        for (Student s : students) {
            if (s instanceof PartTimeStudent) {
                System.out.println(s.getName());
            }
        }
    }

    // 4) Print exam scores of full-time students
    public void printFullTimeExamScores() {
        System.out.println("\n--- Full-Time Exam Scores ---");
        for (Student s : students) {
            if (s instanceof FullTimeStudent ft) {
                System.out.printf("%s -> Exam1: %d, Exam2: %d%n",
                        ft.getName(), ft.getExamScore1(), ft.getExamScore2());
            }
        }
    }
}

public class Main {

    private static int[] generateQuizScores(Random rnd) {
        int[] scores = new int[15];
        for (int i = 0; i < scores.length; i++) {
            scores[i] = 50 + rnd.nextInt(51); // 50-100
        }
        return scores;
    }

    public static void main(String[] args) {
        Session session = new Session();
        Random rnd = new Random();

        // Populate with 20 students (mix of part-time and full-time) + dummy scores
        for (int i = 1; i <= 20; i++) {
            String name = "Student" + i;
            int[] quizzes = generateQuizScores(rnd);

            if (i % 2 == 0) {
                // Full-time student (extra 2 exam scores)
                int exam1 = 50 + rnd.nextInt(51);
                int exam2 = 50 + rnd.nextInt(51);
                session.addStudent(new FullTimeStudent(name, quizzes, exam1, exam2));
            } else {
                // Part-time student
                session.addStudent(new PartTimeStudent(name, quizzes));
            }
        }

        // Call all public methods of Session and capture output on console
        session.printAverageQuizScoresPerStudent();
        session.printAllQuizScoresAscending();
        session.printPartTimeStudentNames();
        session.printFullTimeExamScores();
    }
}
