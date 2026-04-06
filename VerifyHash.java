import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class VerifyHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = "$2b$10$IgwunfhzoNX76OVOOWU1.eKSB7SmLMjNAdTvjYpuY79/U0ebJmcpy";
        String[] candidates = {
                "Demo@2024",
                "123456",
                "Password@123",
                "Student@2024",
                "Admin@2024",
                "cntt123",
                "demo123"
        };
        for (String candidate : candidates) {
            System.out.println(candidate + "=" + encoder.matches(candidate, hash));
        }
    }
}
