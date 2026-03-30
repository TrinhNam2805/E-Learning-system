package com.elearning.validation;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Chính sách mật khẩu theo SRS: độ dài tối thiểu, chữ hoa/thường, số, ký tự đặc biệt.
 */
public final class PasswordPolicy {

    private static final int MIN_LENGTH = 8;
    private static final Pattern HAS_UPPER = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWER = Pattern.compile("[a-z]");
    private static final Pattern HAS_DIGIT = Pattern.compile("[0-9]");
    private static final Pattern HAS_SPECIAL = Pattern.compile("[^a-zA-Z0-9]");

    private PasswordPolicy() {}

    /**
     * @return empty nếu hợp lệ; nếu không — message tiếng Anh hiển thị cho người dùng
     */
    public static Optional<String> validate(String password) {
        if (password == null || password.isEmpty()) {
            return Optional.of("Password is required.");
        }
        if (password.length() < MIN_LENGTH) {
            return Optional.of("Password must be at least " + MIN_LENGTH + " characters.");
        }
        if (!HAS_UPPER.matcher(password).find()) {
            return Optional.of("Password must contain at least one uppercase letter.");
        }
        if (!HAS_LOWER.matcher(password).find()) {
            return Optional.of("Password must contain at least one lowercase letter.");
        }
        if (!HAS_DIGIT.matcher(password).find()) {
            return Optional.of("Password must contain at least one digit.");
        }
        if (!HAS_SPECIAL.matcher(password).find()) {
            return Optional.of("Password must contain at least one special character.");
        }
        return Optional.empty();
    }

    public static String requirementsHint() {
        return "At least " + MIN_LENGTH + " characters, including uppercase, lowercase, a digit, and a special character.";
    }
}
