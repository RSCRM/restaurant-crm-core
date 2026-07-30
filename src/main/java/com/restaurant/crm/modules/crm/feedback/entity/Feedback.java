package com.restaurant.crm.modules.crm.feedback.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.crm.customeraccount.entity.Customer;
import com.restaurant.crm.modules.crm.feedback.constants.FeedbackConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = FeedbackConstants.TABLE_FEEDBACK)
public class Feedback extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = FeedbackConstants.COL_CUSTOMER_ID, nullable = false)
    Customer customer;

    @NotNull
    @Column(name = FeedbackConstants.COL_RESTAURANT_ID, nullable = false)
    String restaurantId;

    @NotNull
    @Column(name = FeedbackConstants.COL_ORDER_ID, nullable = false, unique = true)
    String orderId;

    @NotNull
    @Min(FeedbackConstants.MIN_RATING)
    @Max(FeedbackConstants.MAX_RATING)
    @Column(name = FeedbackConstants.COL_RATING_FOOD, nullable = false)
    Integer ratingFood;

    @NotNull
    @Min(FeedbackConstants.MIN_RATING)
    @Max(FeedbackConstants.MAX_RATING)
    @Column(name = FeedbackConstants.COL_RATING_SERVICE, nullable = false)
    Integer ratingService;

    @Column(name = FeedbackConstants.COL_COMMENT, columnDefinition = FeedbackConstants.COMMENT_DEFINITION)
    String comment;
}
