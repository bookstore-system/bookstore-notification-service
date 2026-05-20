package com.notfound.bookstorenotificationservice.util;

/**
 * Email HTML: ưu tiên đọc nhanh — tiêu đề trong khối nổi bật, phần chi tiết tách rõ; tông giấy mực / cam đất (nhà sách), tránh layout gradient-pill kiểu template chung.
 */
public final class BookstoreNotificationHtmlBuilder {

    private static final String FONT_UI =
            "-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif";

    private static final String FONT_MONO =
            "ui-monospace,'Cascadia Code','Segoe UI Mono',Consolas,monospace";

    private BookstoreNotificationHtmlBuilder() {}

    private static String escapeHtmlUtf8(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    public static String escapeWithLineBreaks(String raw) {
        if (raw == null) {
            return "";
        }
        return escapeHtmlUtf8(raw).replace("\n", "<br>\n");
    }

    public static String wrapNotificationEmail(String subject, String innerHtmlBody) {
        String safeSubject = escapeHtmlUtf8(subject != null ? subject : "Thông báo");
        String body = innerHtmlBody != null ? innerHtmlBody : "";
        String shell = """
                <!DOCTYPE html>
                <html lang="vi">
                <head>
                  <meta charset="UTF-8">
                  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1.0">
                  <title>__TITLE__</title>
                </head>
                <body style="margin:0;padding:0;background:#ede8df;font-family:__FONT_UI__;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#ede8df;padding:32px 14px;font-family:__FONT_UI__;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:540px;border-collapse:separate;border-spacing:0;background:#fffef9;border-radius:4px;overflow:hidden;border:1px solid #d6cfc3;box-shadow:0 12px 28px rgba(28,25,23,0.08);font-family:__FONT_UI__;">
                          <tr>
                            <td style="padding:18px 22px 14px 22px;border-bottom:2px solid #c2410c;background:#fffef9;font-family:__FONT_UI__;">
                              <p style="margin:0 0 4px 0;font-size:15px;font-weight:700;color:#1c1917;letter-spacing:0;">NotFound Bookstore</p>
                              <p style="margin:0;font-size:12px;color:#57534e;line-height:1.45;letter-spacing:0;">Thư từ hệ thống đặt sách — không phải quảng cáo.</p>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:22px 22px 10px 22px;background:#fffef9;font-family:__FONT_UI__;">
                              <p style="margin:0 0 8px 0;font-size:12px;font-weight:700;color:#9a3412;text-transform:none;letter-spacing:0.02em;">Tiêu đề thông báo</p>
                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="border-collapse:collapse;background:#fffbeb;border:1px solid #fcd34d;border-left:6px solid #ea580c;border-radius:0 8px 8px 0;">
                                <tr>
                                  <td style="padding:18px 20px 20px 18px;font-family:__FONT_UI__;">
                                    <p style="margin:0;font-size:24px;font-weight:800;color:#431407;line-height:1.35;letter-spacing:0;">__TITLE__</p>
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:6px 22px 22px 22px;background:#faf7f2;font-family:__FONT_UI__;">
                              <p style="margin:0 0 10px 0;font-size:12px;font-weight:700;color:#57534e;letter-spacing:0;">Chi tiết</p>
                              <div style="font-size:16px;line-height:1.75;color:#292524;font-family:__FONT_UI__;letter-spacing:0;padding:16px 18px;background:#ffffff;border:1px solid #e7e5e4;border-radius:8px;">
                __BODY__
                              </div>
                            </td>
                          </tr>
                          <tr>
                            <td style="padding:16px 22px 20px 22px;background:#f5f0e9;border-top:1px solid #d6d3d1;font-family:__FONT_UI__;">
                              <p style="margin:0;font-size:12px;color:#57534e;line-height:1.65;letter-spacing:0;">Nếu bạn không chờ thông báo này, có thể bỏ qua. Mọi giao dịch đặt sách vẫn được quản lý trên tài khoản của bạn tại NotFound.</p>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """;
        return shell
                .replace("__FONT_UI__", FONT_UI)
                .replace("__TITLE__", safeSubject)
                .replace("__BODY__", body);
    }

    public static String buildOrderNotificationBody(
            String customerName,
            String orderId,
            String status,
            String totalPrice) {
        String name = customerName != null && !customerName.isBlank() ? customerName : "Quý khách";
        String safeName = escapeHtmlUtf8(name);
        String safeOrder = escapeHtmlUtf8(orderId != null ? orderId : "—");
        String safeStatus = escapeHtmlUtf8(status != null ? status : "—");
        String priceLine = totalPrice != null && !totalPrice.isBlank()
                ? "<p style=\"margin:14px 0 0 0;font-family:" + FONT_UI + ";font-size:15px;color:#292524;\"><strong style=\"color:#1c1917;\">Tổng tiền:</strong> "
                        + escapeHtmlUtf8(totalPrice) + "</p>"
                : "";
        return """
                <p style="margin:0 0 12px 0;font-family:__FONT_UI__;font-size:16px;line-height:1.7;color:#292524;letter-spacing:0;">Xin chào <strong style="color:#1c1917;">__NAME__</strong>,</p>
                <p style="margin:0 0 16px 0;font-family:__FONT_UI__;font-size:16px;line-height:1.7;color:#44403c;letter-spacing:0;">Dưới đây là thông tin đơn hàng được cập nhật:</p>
                <table role="presentation" cellpadding="0" cellspacing="0" style="width:100%%;border-collapse:separate;border-spacing:0;margin:0;background:#fffef9;border-radius:8px;overflow:hidden;border:1px solid #d6d3d1;font-family:__FONT_UI__;">
                  <tr>
                    <td style="padding:14px 16px;font-size:13px;font-weight:700;color:#57534e;font-family:__FONT_UI__;">Mã đơn</td>
                    <td style="padding:14px 16px;font-size:13px;color:#1c1917;text-align:right;font-family:__FONT_MONO__;font-weight:600;">__ORDER__</td>
                  </tr>
                  <tr>
                    <td style="padding:14px 16px;border-top:1px solid #e7e5e4;font-size:13px;font-weight:700;color:#57534e;font-family:__FONT_UI__;">Trạng thái</td>
                    <td style="padding:14px 16px;border-top:1px solid #e7e5e4;font-size:15px;color:#c2410c;text-align:right;font-weight:800;font-family:__FONT_UI__;">__STATUS__</td>
                  </tr>
                </table>
                __PRICE__
                """
                .replace("__FONT_UI__", FONT_UI)
                .replace("__FONT_MONO__", FONT_MONO)
                .replace("__NAME__", safeName)
                .replace("__ORDER__", safeOrder)
                .replace("__STATUS__", safeStatus)
                .replace("__PRICE__", priceLine);
    }

    public static String buildPaymentNotificationBody(
            String customerName,
            String paymentId,
            String orderId,
            String amount,
            String currency,
            String status,
            String paymentMethod) {
        String name = customerName != null && !customerName.isBlank() ? customerName : "Quý khách";
        String safeName = escapeHtmlUtf8(name);
        String safePayment = escapeHtmlUtf8(paymentId != null ? paymentId : "—");
        String safeOrder = escapeHtmlUtf8(orderId != null ? orderId : "—");
        String safeAmount = escapeHtmlUtf8(amount != null ? amount : "—");
        String safeCurrency = escapeHtmlUtf8(currency != null && !currency.isBlank() ? currency : "VND");
        String safeStatus = escapeHtmlUtf8(status != null ? status : "—");
        String methodRow = paymentMethod != null && !paymentMethod.isBlank()
                ? """
                  <tr>
                    <td style="padding:14px 16px;border-top:1px solid #e7e5e4;font-size:13px;font-weight:700;color:#57534e;font-family:__FONT_UI__;">Phương thức</td>
                    <td style="padding:14px 16px;border-top:1px solid #e7e5e4;font-size:14px;color:#1c1917;text-align:right;font-weight:600;font-family:__FONT_UI__;">__METHOD__</td>
                  </tr>
                  """
                        .replace("__FONT_UI__", FONT_UI)
                        .replace("__METHOD__", escapeHtmlUtf8(paymentMethod))
                : "";
        return """
                <p style="margin:0 0 12px 0;font-family:__FONT_UI__;font-size:16px;line-height:1.7;color:#292524;letter-spacing:0;">Xin chào <strong style="color:#1c1917;">__NAME__</strong>,</p>
                <p style="margin:0 0 16px 0;font-family:__FONT_UI__;font-size:16px;line-height:1.7;color:#44403c;letter-spacing:0;">Chúng tôi ghi nhận một giao dịch thanh toán liên quan đơn hàng của bạn:</p>
                <table role="presentation" cellpadding="0" cellspacing="0" style="width:100%%;border-collapse:separate;border-spacing:0;margin:0;background:#fffef9;border-radius:8px;overflow:hidden;border:1px solid #d6d3d1;font-family:__FONT_UI__;">
                  <tr>
                    <td style="padding:14px 16px;font-size:13px;font-weight:700;color:#57534e;font-family:__FONT_UI__;">Mã giao dịch</td>
                    <td style="padding:14px 16px;font-size:13px;color:#1c1917;text-align:right;font-family:__FONT_MONO__;font-weight:600;">__PAYMENT__</td>
                  </tr>
                  <tr>
                    <td style="padding:14px 16px;border-top:1px solid #e7e5e4;font-size:13px;font-weight:700;color:#57534e;font-family:__FONT_UI__;">Mã đơn</td>
                    <td style="padding:14px 16px;border-top:1px solid #e7e5e4;font-size:13px;color:#1c1917;text-align:right;font-family:__FONT_MONO__;font-weight:600;">__ORDER__</td>
                  </tr>
                  <tr>
                    <td style="padding:14px 16px;border-top:1px solid #e7e5e4;font-size:13px;font-weight:700;color:#57534e;font-family:__FONT_UI__;">Số tiền</td>
                    <td style="padding:14px 16px;border-top:1px solid #e7e5e4;font-size:15px;color:#c2410c;text-align:right;font-weight:800;font-family:__FONT_UI__;">__AMOUNT__ __CURRENCY__</td>
                  </tr>
                  <tr>
                    <td style="padding:14px 16px;border-top:1px solid #e7e5e4;font-size:13px;font-weight:700;color:#57534e;font-family:__FONT_UI__;">Trạng thái</td>
                    <td style="padding:14px 16px;border-top:1px solid #e7e5e4;font-size:15px;color:#1c1917;text-align:right;font-weight:800;font-family:__FONT_UI__;">__STATUS__</td>
                  </tr>
                  __METHOD_ROW__
                </table>
                """
                .replace("__FONT_UI__", FONT_UI)
                .replace("__FONT_MONO__", FONT_MONO)
                .replace("__NAME__", safeName)
                .replace("__PAYMENT__", safePayment)
                .replace("__ORDER__", safeOrder)
                .replace("__AMOUNT__", safeAmount)
                .replace("__CURRENCY__", safeCurrency)
                .replace("__STATUS__", safeStatus)
                .replace("__METHOD_ROW__", methodRow);
    }

    /**
     * @param resetLinkHref liên kết đặt lại (đưa vào thuộc tính {@code href}, đã escape HTML)
     */
    public static String buildCheckoutCompletedBody(
            String customerName,
            String sagaId,
            String orderId,
            String totalPrice) {
        return buildSagaStatusBody(
                customerName,
                "Cảm ơn bạn đã đặt hàng tại NotFound Bookstore. Đơn hàng đã được xử lý thành công và đang được chuẩn bị.",
                sagaId,
                orderId,
                "Hoàn tất",
                totalPrice != null ? "Tổng thanh toán: " + totalPrice : null,
                "#15803d");
    }

    public static String buildCheckoutFailedBody(
            String customerName,
            String sagaId,
            String orderId,
            String failureReason) {
        String reasonLine = failureReason != null && !failureReason.isBlank()
                ? "Lý do: " + failureReason
                : "Vui lòng thử lại sau hoặc chọn sách khác.";
        return buildSagaStatusBody(
                customerName,
                "Rất tiếc, quá trình thanh toán / đặt hàng chưa hoàn tất.",
                sagaId,
                orderId,
                "Thất bại",
                reasonLine,
                "#b91c1c");
    }

    public static String buildPaymentCompletedSagaBody(
            String customerName,
            String sagaId,
            String orderId,
            String paymentId,
            String amount,
            String currency,
            String paymentMethod) {
        String name = customerName != null && !customerName.isBlank() ? customerName : "Quý khách";
        String safeName = escapeHtmlUtf8(name);
        String methodRow = paymentMethod != null && !paymentMethod.isBlank()
                ? row("Phương thức", escapeHtmlUtf8(paymentMethod))
                : "";
        return """
                <p style="margin:0 0 12px 0;font-family:__FONT_UI__;font-size:16px;line-height:1.7;color:#292524;">Xin chào <strong style="color:#1c1917;">__NAME__</strong>,</p>
                <p style="margin:0 0 16px 0;font-family:__FONT_UI__;font-size:16px;line-height:1.7;color:#44403c;">Thanh toán của bạn đã được ghi nhận thành công.</p>
                <table role="presentation" cellpadding="0" cellspacing="0" style="width:100%%;border-collapse:separate;border-spacing:0;background:#fffef9;border-radius:8px;border:1px solid #d6d3d1;font-family:__FONT_UI__;">
                  __SAGA_ROW__
                  __ORDER_ROW__
                  __PAYMENT_ROW__
                  __AMOUNT_ROW__
                  __METHOD_ROW__
                </table>
                """
                .replace("__FONT_UI__", FONT_UI)
                .replace("__NAME__", safeName)
                .replace("__SAGA_ROW__", rowFirst("Mã checkout (saga)", escapeHtmlUtf8(sagaId != null ? sagaId : "—")))
                .replace("__ORDER_ROW__", row("Mã đơn", escapeHtmlUtf8(orderId != null ? orderId : "—")))
                .replace("__PAYMENT_ROW__", row("Mã giao dịch", escapeHtmlUtf8(paymentId != null ? paymentId : "—")))
                .replace(
                        "__AMOUNT_ROW__",
                        row(
                                "Số tiền",
                                escapeHtmlUtf8(amount != null ? amount : "—")
                                        + " "
                                        + escapeHtmlUtf8(currency != null && !currency.isBlank() ? currency : "VND")))
                .replace("__METHOD_ROW__", methodRow);
    }

    public static String buildOrderCancelledBody(
            String customerName,
            String sagaId,
            String orderId,
            String reason) {
        String detail = reason != null && !reason.isBlank()
                ? reason
                : "Đơn hàng đã được hủy theo yêu cầu hệ thống hoặc do lỗi xử lý.";
        return buildSagaStatusBody(
                customerName,
                "Đơn hàng của bạn đã được hủy.",
                sagaId,
                orderId,
                "Đã hủy",
                detail,
                "#b45309");
    }

    private static String buildSagaStatusBody(
            String customerName,
            String intro,
            String sagaId,
            String orderId,
            String statusLabel,
            String extraLine,
            String statusColor) {
        String name = customerName != null && !customerName.isBlank() ? customerName : "Quý khách";
        String extra = extraLine != null && !extraLine.isBlank()
                ? "<p style=\"margin:14px 0 0 0;font-size:15px;color:#44403c;font-family:"
                        + FONT_UI
                        + ";\">"
                        + escapeHtmlUtf8(extraLine)
                        + "</p>"
                : "";
        return """
                <p style="margin:0 0 12px 0;font-family:__FONT_UI__;font-size:16px;line-height:1.7;color:#292524;">Xin chào <strong style="color:#1c1917;">__NAME__</strong>,</p>
                <p style="margin:0 0 16px 0;font-family:__FONT_UI__;font-size:16px;line-height:1.7;color:#44403c;">__INTRO__</p>
                <table role="presentation" cellpadding="0" cellspacing="0" style="width:100%%;border-collapse:separate;border-spacing:0;background:#fffef9;border-radius:8px;border:1px solid #d6d3d1;font-family:__FONT_UI__;">
                  __SAGA_ROW__
                  __ORDER_ROW__
                  <tr>
                    <td style="padding:14px 16px;border-top:1px solid #e7e5e4;font-size:13px;font-weight:700;color:#57534e;">Trạng thái</td>
                    <td style="padding:14px 16px;border-top:1px solid #e7e5e4;font-size:15px;color:__STATUS_COLOR__;text-align:right;font-weight:800;">__STATUS__</td>
                  </tr>
                </table>
                __EXTRA__
                """
                .replace("__FONT_UI__", FONT_UI)
                .replace("__NAME__", escapeHtmlUtf8(name))
                .replace("__INTRO__", escapeHtmlUtf8(intro))
                .replace("__SAGA_ROW__", rowFirst("Mã checkout (saga)", escapeHtmlUtf8(sagaId != null ? sagaId : "—")))
                .replace("__ORDER_ROW__", row("Mã đơn", escapeHtmlUtf8(orderId != null ? orderId : "—")))
                .replace("__STATUS_COLOR__", statusColor)
                .replace("__STATUS__", escapeHtmlUtf8(statusLabel))
                .replace("__EXTRA__", extra);
    }

    private static String rowFirst(String label, String valueHtml) {
        return """
                <tr>
                  <td style="padding:14px 16px;font-size:13px;font-weight:700;color:#57534e;font-family:__FONT_UI__;">__LABEL__</td>
                  <td style="padding:14px 16px;font-size:13px;color:#1c1917;text-align:right;font-family:__FONT_MONO__;font-weight:600;">__VALUE__</td>
                </tr>
                """
                .replace("__FONT_UI__", FONT_UI)
                .replace("__FONT_MONO__", FONT_MONO)
                .replace("__LABEL__", escapeHtmlUtf8(label))
                .replace("__VALUE__", valueHtml);
    }

    private static String row(String label, String valueHtml) {
        return """
                <tr>
                  <td style="padding:14px 16px;border-top:1px solid #e7e5e4;font-size:13px;font-weight:700;color:#57534e;font-family:__FONT_UI__;">__LABEL__</td>
                  <td style="padding:14px 16px;border-top:1px solid #e7e5e4;font-size:13px;color:#1c1917;text-align:right;font-family:__FONT_MONO__;font-weight:600;">__VALUE__</td>
                </tr>
                """
                .replace("__FONT_UI__", FONT_UI)
                .replace("__FONT_MONO__", FONT_MONO)
                .replace("__LABEL__", escapeHtmlUtf8(label))
                .replace("__VALUE__", valueHtml);
    }

    public static String buildPasswordResetBody(String displayName, String resetLinkHref, int expiresInMinutes) {
        String name = displayName != null && !displayName.isBlank() ? displayName : "Quý khách";
        String safeName = escapeHtmlUtf8(name);
        String safeHref = resetLinkHref != null ? resetLinkHref : "";
        String expiryText = expiresInMinutes > 0
                ? "Liên kết có hiệu lực trong khoảng <strong style=\"color:#1c1917;\">"
                        + escapeHtmlUtf8(String.valueOf(expiresInMinutes))
                        + "</strong> phút."
                : "Liên kết chỉ dùng một lần và sẽ hết hạn sớm — vui lòng thao tác ngay.";
        return """
                <p style="margin:0 0 12px 0;font-family:__FONT_UI__;font-size:16px;line-height:1.7;color:#292524;letter-spacing:0;">Xin chào <strong style="color:#1c1917;">__NAME__</strong>,</p>
                <p style="margin:0 0 14px 0;font-family:__FONT_UI__;font-size:16px;line-height:1.7;color:#44403c;letter-spacing:0;">Bạn (hoặc ai đó) vừa yêu cầu đặt lại mật khẩu cho tài khoản NotFound Bookstore. Nếu đúng là bạn, hãy bấm nút bên dưới.</p>
                <table role="presentation" cellpadding="0" cellspacing="0" style="margin:18px 0 20px 0;">
                  <tr>
                    <td align="center" style="border-radius:8px;background:#c2410c;">
                      <a href="__HREF__" style="display:inline-block;padding:14px 28px;font-family:__FONT_UI__;font-size:16px;font-weight:800;color:#fffef9;text-decoration:none;letter-spacing:0;">Đặt lại mật khẩu</a>
                    </td>
                  </tr>
                </table>
                <p style="margin:0 0 12px 0;font-family:__FONT_UI__;font-size:14px;line-height:1.65;color:#57534e;letter-spacing:0;">__EXPIRY__</p>
                <p style="margin:0;font-family:__FONT_UI__;font-size:13px;line-height:1.65;color:#78716c;letter-spacing:0;">Nếu bạn không yêu cầu thao tác này, hãy bỏ qua email và mật khẩu vẫn an toàn. Không chia sẻ liên kết này cho người khác.</p>
                """
                .replace("__FONT_UI__", FONT_UI)
                .replace("__NAME__", safeName)
                .replace("__HREF__", safeHref)
                .replace("__EXPIRY__", expiryText);
    }
}
