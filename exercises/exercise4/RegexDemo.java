import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexDemo {

    // Helper to run one test and print PASS/FAIL clearly
    private static void test(String title, String regex, String subject, boolean expectedMatch) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(subject);
        boolean matched = m.matches();

        System.out.println("--------------------------------------------------");
        System.out.println("Pattern: " + title);
        System.out.println("Regex  : " + regex);
        System.out.println("Subject: \"" + subject + "\"");
        System.out.println("Expected: " + (expectedMatch ? "MATCH" : "NO MATCH"));
        System.out.println("Actual  : " + (matched ? "MATCH" : "NO MATCH"));
        System.out.println("Result  : " + ((matched == expectedMatch) ? "PASS" : "FAIL"));

        // If matched, show groups (if any)
        if (matched) {
            int groups = m.groupCount();
            if (groups > 0) {
                for (int i = 1; i <= groups; i++) {
                    System.out.println("Group " + i + ": " + m.group(i));
                }
            }
        }
    }

    public static void main(String[] args) {

        // 1) Email validation (simple, not fully RFC-complete)
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        test("Email (basic)", emailRegex, "john.doe99@gmail.com", true);     // positive
        test("Email (basic)", emailRegex, "john..doe@@gmail", false);        // negative

        // 2) US phone format: (123) 456-7890 OR 123-456-7890
        String phoneRegex = "^(\\(\\d{3}\\)\\s?\\d{3}-\\d{4}|\\d{3}-\\d{3}-\\d{4})$";
        test("US Phone", phoneRegex, "(408) 555-1234", true);                // positive
        test("US Phone", phoneRegex, "4085551234", false);                   // negative

        // 3) Password rule: min 8 chars, at least 1 uppercase, 1 lowercase, 1 digit
        // Uses lookaheads
        String passwordRegex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";
        test("Password (8+, upper+lower+digit)", passwordRegex, "Abcdefg1", true); // positive
        test("Password (8+, upper+lower+digit)", passwordRegex, "abcdefg1", false);// negative (no uppercase)

        // 4) Date format: YYYY-MM-DD with month 01-12 and day 01-31 (range check, not calendar-accurate)
        String dateRegex = "^(\\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$";
        test("Date YYYY-MM-DD (range)", dateRegex, "2025-12-13", true);      // positive
        test("Date YYYY-MM-DD (range)", dateRegex, "2025-13-40", false);     // negative

        // 5) IPv4 address (0-255 per octet)
        String ipv4Regex =
                "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)\\.){3}" +
                        "(25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)$";
        test("IPv4", ipv4Regex, "192.168.0.1", true);                        // positive
        test("IPv4", ipv4Regex, "256.168.0.1", false);                       // negative

        // 6) Optional extra (still fine if you keep 5, but this shows capturing groups nicely):
        // Extract "Last, First" with optional middle initial like "Xu, Ruixuan" or "Smith, John A."
        String nameRegex = "^([A-Za-z]+),\\s([A-Za-z]+)(\\s[A-Za-z]\\.)?$";
        test("Name: Last, First (optional MI)", nameRegex, "Smith, John A.", true); // positive
        test("Name: Last, First (optional MI)", nameRegex, "John Smith", false);    // negative

        System.out.println("==================================================");
        System.out.println("Done. 5+ regex patterns tested with positive + negative cases.");
    }
}
