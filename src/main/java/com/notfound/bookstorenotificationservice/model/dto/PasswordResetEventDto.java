package com.notfound.bookstorenotificationservice.model.dto;

import java.util.UUID;

/**
 * Sự kiện quên mật khẩu từ user/auth-service: không có tổng tiền hay order — cần liên kết đặt lại, thời hạn, cảnh báo bảo mật.
 */
public class PasswordResetEventDto {

    private UUID userId;
    private String email;
    private String displayName;
    /** URL đầy đủ (https://...) dẫn tới trang đặt lại mật khẩu kèm token. */
    private String resetLink;
    /** Thời gian hiệu lực (phút), hiển thị trong email. */
    private Integer expiresInMinutes;

    public PasswordResetEventDto() {}

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getResetLink() {
        return resetLink;
    }

    public void setResetLink(String resetLink) {
        this.resetLink = resetLink;
    }

    public Integer getExpiresInMinutes() {
        return expiresInMinutes;
    }

    public void setExpiresInMinutes(Integer expiresInMinutes) {
        this.expiresInMinutes = expiresInMinutes;
    }

    @Override
    public String toString() {
        return "PasswordResetEventDto{"
                + "userId=" + userId
                + ", email='" + (email != null ? "***" : null) + '\''
                + ", displayName='" + displayName + '\''
                + ", resetLink=" + (resetLink != null ? "[present]" : "null")
                + ", expiresInMinutes=" + expiresInMinutes
                + '}';
    }
}
