# Hệ thống Đa Ngôn Ngữ (i18n) - E-Learning CNTT

## Giới Thiệu

Hệ thống đa ngôn ngữ này cho phép ứng dụng hiển thị nội dung bằng **Tiếng Anh** (mặc định) hoặc **Tiếng Việt** dựa trên lựa chọn người dùng.

## Cấu Trúc Thư Mục

```
mockup/
├── js/
│   ├── i18n.js              # Logic quản lý ngôn ngữ
│   └── translations.json    # Dữ liệu bản dịch (EN & VI)
├── css/
│   └── style.css            # CSS (bao gồm language toggle button)
├── mockData/
│   └── users.json           # Dữ liệu người dùng mẫu
└── *.html                   # Các trang HTML
```

## Cách Hoạt Động

### 1. **Tệp Dịch (translations.json)**
- Chứa tất cả các chuỗi text cho cả Tiếng Anh và Tiếng Việt
- Cấu trúc: `{ "en": {...}, "vi": {...} }`
- Sử dụng **key-value pairs** với naming convention: `section.key` (ví dụ: `hero.title`)

### 2. **Hệ Thống i18n (i18n.js)**
- **Khởi tạo**: Tải file `translations.json` khi trang load
- **Lưu Trững**: Lưu ngôn ngữ được chọn vào `localStorage`
- **Cập Nhật**: Cập nhật tất cả phần tử có thuộc tính `data-i18n`

### 3. **Sử Dụng Trong HTML**
Thêm thuộc tính `data-i18n="key"` vào phần tử HTML:

```html
<h1 data-i18n="hero.title">Welcome to E-Learning CNTT</h1>
<button data-i18n="hero.button">Join Now</button>
```

### 4. **Language Toggle Button**
- Nút chuyển đổi ngôn ngữ (🇬🇧 / 🇻🇳) trong navigation
- Click để chuyển giữa Tiếng Anh và Tiếng Việt
- Ngôn ngữ được chọn tự lưu vào `localStorage`

## Cách Thêm Dịch Mới

### Bước 1: Thêm key trong `translations.json`
```json
{
  "en": {
    "new.key": "English text"
  },
  "vi": {
    "new.key": "Văn bản tiếng Việt"
  }
}
```

### Bước 2: Sử dụng trong HTML
```html
<p data-i18n="new.key">English text</p>
```

## Chuyển Sang Spring Boot + Thymeleaf

Khi chuyển sang Spring Boot + Thymeleaf, cấu trúc i18n này sẽ dễ dàng tích hợp:

### Spring Boot i18n Configuration:

```java
// application.properties
spring.messages.basename=messages
spring.messages.encoding=UTF-8

// Hoặc trong application.yml
spring:
  messages:
    basename: messages
    encoding: UTF-8
```

### Tệp Dịch Spring Boot:
```
src/main/resources/
├── messages_en.properties     # (từ translations.json - "en" section)
├── messages_vi.properties     # (từ translations.json - "vi" section)
└── messages.properties        # Mặc định (EN)
```

### Sử Dụng trong Thymeleaf:
```html
<!-- Thymeleaf i18n -->
<h1 th:text="#{hero.title}">Welcome</h1>
<button th:text="#{hero.button}">Join Now</button>

<!-- Language Toggle -->
<a th:href="@{/?lang=en}">English</a>
<a th:href="@{/?lang=vi}">Việt Nam</a>
```

### Java Controller:
```java
@GetMapping("/")
public String home(Locale locale, Model model) {
    // Spring tự xử lý locale từ request parameter
    return "homepage";
}
```

## Lợi Ích Của Hệ Thống Này

✅ **Dễ Bảo Trì**: Tất cả text dịch được tập trung trong một file  
✅ **Dễ Mở Rộng**: Thêm ngôn ngữ mới chỉ cần thêm một section  
✅ **Tương Thích Spring Boot**: Cấu trúc giống Spring Boot's ResourceBundleMessageSource  
✅ **Lưu Trữ Tùy Chọn**: Ngôn ngữ được lưu và nhớ lại khi quay lại  
✅ **Performance**: Tải một lần, không cần request server mỗi lần chuyển ngôn ngữ  

## Ngôn Ngữ Mặc Định

- **Hiện tại**: Tiếng Anh (EN)
- Người dùng có thể chuyển sang Tiếng Việt bằng nút toggle

## Testing

Mở browser console và thử:
```javascript
i18n.setLanguage('vi');      // Chuyển sang Tiếng Việt
i18n.setLanguage('en');      // Chuyển sang Tiếng Anh
i18n.getCurrentLanguage();   // Kiểm tra ngôn ngữ hiện tại
```
