package com.notfound.bookstorenotificationservice.model.dto;

public class NotificationPreferencesResponse {

    private boolean promotionEmail;

    public NotificationPreferencesResponse() {
    }

    public NotificationPreferencesResponse(boolean promotionEmail) {
        this.promotionEmail = promotionEmail;
    }

    public boolean isPromotionEmail() {
        return promotionEmail;
    }

    public void setPromotionEmail(boolean promotionEmail) {
        this.promotionEmail = promotionEmail;
    }
}

