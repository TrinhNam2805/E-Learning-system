package com.elearning.service;

import com.elearning.exception.AssessmentNotFoundException;
import com.elearning.exception.AssessmentStorageException;
import com.elearning.exception.AssessmentValidationException;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class AssessmentFileStorageService {

    private static final DateTimeFormatter FILE_TIMESTAMP = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final Path uploadRoot;
    private final Set<String> allowedExtensions;
    private final long maxFileSizeBytes;

    public AssessmentFileStorageService(
            @Value("${app.assessment.upload-dir:uploads/assessment}") String uploadDir,
            @Value("${app.assessment.allowed-extensions:pdf,doc,docx,txt,zip}") String allowedExtensions,
            @Value("${app.assessment.max-file-size-bytes:5242880}") long maxFileSizeBytes) {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.allowedExtensions = parseExtensions(allowedExtensions);
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public StoredFile storeSubmissionFile(Long assignmentId, Long studentId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        if (!StringUtils.hasText(originalFileName)) {
            throw new AssessmentValidationException("The uploaded file is invalid.");
        }

        String extension = extractExtension(originalFileName);
        if (!allowedExtensions.contains(extension)) {
            throw new AssessmentValidationException("Unsupported file format. Allowed formats: " + allowedExtensions);
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new AssessmentValidationException("The file size exceeds the limit of " + readableSize(maxFileSizeBytes) + ".");
        }

        try {
            Files.createDirectories(uploadRoot);
            String storedFileName = buildStoredFileName(assignmentId, studentId, extension);
            Path target = uploadRoot.resolve(storedFileName).normalize();
            if (!target.startsWith(uploadRoot)) {
                throw new AssessmentValidationException("The file path is invalid.");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredFile(storedFileName, originalFileName, file.getSize());
        } catch (IOException ex) {
            throw new AssessmentStorageException("The submission file cannot be saved right now.", ex);
        }
    }

    public Resource loadAsResource(String storedFileName) {
        if (!StringUtils.hasText(storedFileName)) {
            throw new AssessmentNotFoundException("The submission file could not be found.");
        }

        try {
            Path filePath = uploadRoot.resolve(storedFileName).normalize();
            if (!filePath.startsWith(uploadRoot)) {
                throw new AssessmentValidationException("The file path is invalid.");
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new AssessmentNotFoundException("The submission file could not be found.");
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new AssessmentStorageException("The submission file could not be read.", ex);
        }
    }

    public void deleteSubmissionFile(String storedFileName) {
        if (!StringUtils.hasText(storedFileName)) {
            return;
        }

        try {
            Path filePath = uploadRoot.resolve(storedFileName).normalize();
            if (!filePath.startsWith(uploadRoot)) {
                throw new AssessmentValidationException("The file path is invalid.");
            }
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new AssessmentStorageException("The submission file could not be removed right now.", ex);
        }
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public Set<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    private Set<String> parseExtensions(String csv) {
        Set<String> values = new HashSet<String>();
        String[] items = csv.split(",");
        for (String item : items) {
            String normalized = item == null ? "" : item.trim().toLowerCase(Locale.ROOT);
            if (!normalized.isEmpty()) {
                values.add(normalized);
            }
        }
        return values;
    }

    private String extractExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String buildStoredFileName(Long assignmentId, Long studentId, String extension) {
        String timestamp = LocalDateTime.now().format(FILE_TIMESTAMP);
        return "assignment-" + assignmentId
                + "-student-" + studentId
                + "-" + timestamp
                + "-" + UUID.randomUUID().toString().replace("-", "")
                + "." + extension;
    }

    private String readableSize(long sizeBytes) {
        if (sizeBytes >= 1024 * 1024) {
            return (sizeBytes / (1024 * 1024)) + " MB";
        }
        if (sizeBytes >= 1024) {
            return (sizeBytes / 1024) + " KB";
        }
        return sizeBytes + " B";
    }

    @Getter
    public static class StoredFile {
        private final String storedFileName;
        private final String originalFileName;
        private final long size;

        public StoredFile(String storedFileName, String originalFileName, long size) {
            this.storedFileName = storedFileName;
            this.originalFileName = originalFileName;
            this.size = size;
        }
    }
}
