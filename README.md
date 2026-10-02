# HỆ THỐNG HỒ SƠ SỨC KHỎE ĐIỆN TỬ
**Sinh viên thực hiện:** Huỳnh Khánh Linh  
**MSSV:** B2306627

---

## 1. Yêu cầu môi trường
*   **Java 17+, Node.js 18+, Python 3.9+**
*   **Cơ sở dữ liệu:** PostgreSQL (phiên bản 14+)

---

## 2. Hướng dẫn khởi chạy

### Bước 1: Thiết lập Cơ sở dữ liệu và Môi trường (.env)
1. Khởi tạo một database trống trên PostgreSQL (ví dụ tên: `healthrecord`).
2. **Cấu hình tệp .env:** Trong thư mục gốc dự án, hãy sao chép tệp `.env` thành tệp `.env` và cập nhật các thông số sau:
   *   **Database:** `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
   *   **Khóa bảo mật:** `JWT_SECRET` (Chuỗi ký tự dài ít nhất 32 ký tự).
   *   **Khóa API:** `GEMINI_API_KEY` (Cho chức năng AI Chat), `CLOUDINARY_API_KEY` (Cho chức năng Upload ảnh).
3. Hệ thống đã cấu hình tự động tạo cấu trúc bảng (Hibernate DDL-Auto), các bảng sẽ được sinh ra khi Backend khởi chạy lần đầu.

### Bước 2: Khởi chạy AI Server (Dự đoán Tim mạch)
*   Mở terminal tại thư mục `/ai`.
*   Cài đặt thư viện: `pip install -r requirements.txt`
*   Chạy server: `python server/ai_server.py` (Mặc định chạy cổng **5000**)

### Bước 3: Khởi chạy Backend (Spring Boot)
Bạn có thể chọn một trong hai cách sau:
*   **Cách 1 (Dùng IDE):** Mở dự án bằng IntelliJ IDEA, đợi Maven tải xong các dependency, sau đó nhấn nút **Run** (hình tam giác xanh) tại file `HealthRecordApplication.java`.
*   **Cách 2 (Dùng Terminal):** Mở terminal tại thư mục gốc dự án và chạy lệnh:
    ```bash
    mvnw spring-boot:run
    ```
    *(Mặc định Backend chạy cổng **8080**)*

### Bước 4: Khởi chạy Frontend (Vue.js)
*   Mở terminal tại thư mục `/frontend`.
*   Cài đặt thư viện: `npm install`
*   Chạy giao diện: `npm run dev` (Mặc định chạy cổng **5173**)

---

## 3. Lưu ý về tài khoản
Hệ thống sử dụng cơ chế phân quyền giữa **USER** và **ADMIN**. Mọi tài khoản mới đăng ký mặc định sẽ có quyền **USER**. 

Để có quyền quản trị viên (Admin), vui lòng thực hiện các bước sau:
1. Truy cập **http://localhost:5173** và tiến hành **Đăng ký** tài khoản mới.
2. Mở trình quản lý cơ sở dữ liệu (ví dụ: pgAdmin) và chạy lệnh SQL sau để nâng cấp quyền:
   ```sql
   UPDATE users SET role = 'ADMIN' WHERE email = 'email_vừa_đăng_ký@gmail.com';
   ```
3. Sau khi thực hiện lệnh trên, hãy đăng nhập lại để truy cập vào các chức năng quản trị viên.
