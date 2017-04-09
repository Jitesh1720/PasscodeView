package com.kevalpatel.passcodeview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.fingerprint.FingerprintManager;
import android.support.annotation.ColorInt;
import android.support.annotation.Dimension;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextPaint;
import android.view.View;
import android.view.animation.CycleInterpolator;

/**
 * Created by Keval on 07-Apr-17.
 *
 * @author 'https://github.com/kevalpatel2106'
 */

class BoxFingerprint extends Box implements FingerPrintAuthHelper.FingerPrintAuthCallback {
    static final String DEF_FINGERPRINT_STATUS = "Scan your finger to authenticate";

    private Boolean isFingerPrintBoxVisible;
    private Rect mBounds = new Rect();

    @ColorInt
    private int mStatusTextColor;
    @Dimension
    private float mStatusTextSize;
    private String mNormalStatusText;

    private String mCurrentStatusText;

    private TextPaint mStatusTextPaint;

    @Nullable
    private FingerPrintAuthHelper mFingerPrintAuthHelper;

    BoxFingerprint(@NonNull View view) {
        super(view);
        isFingerPrintBoxVisible = FingerPrintUtils.isFingerPrintEnrolled(getContext());

        enableFingerprintScanner();
    }

    private void enableFingerprintScanner() {
        if (!isFingerPrintBoxVisible) return;

        mFingerPrintAuthHelper = FingerPrintAuthHelper.getHelper(getContext(), this);
        mFingerPrintAuthHelper.startAuth();
    }

    void stopFingerprintScanner() {
        if (mFingerPrintAuthHelper != null) mFingerPrintAuthHelper.stopAuth();
    }

    @SuppressWarnings("deprecation")
    void setDefaults() {
        mStatusTextSize = getContext().getResources().getDimension(R.dimen.fingerprint_status_text_size);
        mNormalStatusText = DEF_FINGERPRINT_STATUS;
        mCurrentStatusText = mNormalStatusText;
        mStatusTextColor = getContext().getResources().getColor(R.color.key_default_color);
    }

    @Override
    void onAuthenticationFail() {
        playErrorAnimation();
    }

    @Override
    void onAuthenticationSuccess() {
        //TODO How to notify the user.
    }

    @SuppressWarnings("deprecation")
    @Override
    void draw(@NonNull Canvas canvas) {
        if (isFingerPrintBoxVisible) {
            canvas.drawLine(mBounds.left + getContext().getResources().getDimension(R.dimen.divider_horizontal_margin),
                    mBounds.top,
                    mBounds.right - getContext().getResources().getDimension(R.dimen.divider_horizontal_margin),
                    mBounds.top,
                    mStatusTextPaint);

            //Show fingerprint icon
            Drawable d = getContext().getResources().getDrawable(R.drawable.ic_fingerprint);
            d.setBounds((int) (mBounds.exactCenterX() - mBounds.height() / 4),
                    mBounds.top + 15,
                    (int) (mBounds.exactCenterX() + mBounds.height() / 4),
                    mBounds.top + mBounds.height() / 2 + 15);
            d.setColorFilter(new PorterDuffColorFilter(mStatusTextPaint.getColor(), PorterDuff.Mode.SRC_ATOP));
            d.draw(canvas);

            //Show finger print text
            canvas.drawText(mCurrentStatusText,
                    mBounds.exactCenterX(),
                    (float) (mBounds.top + (mBounds.height() / 1.3) - ((mStatusTextPaint.descent() + mStatusTextPaint.ascent()) / 2)),
                    mStatusTextPaint);
        }
    }

    @Override
    void measure(@NonNull Rect rootViewBounds) {
        if (isFingerPrintBoxVisible) {
            mBounds.left = rootViewBounds.left;
            mBounds.right = rootViewBounds.right;
            mBounds.top = (int) (rootViewBounds.bottom - rootViewBounds.height() * (BoxKeypad.KEY_BOARD_BOTTOM_WEIGHT));
            mBounds.bottom = rootViewBounds.bottom;
        }
    }

    @Override
    void preparePaint() {
        mStatusTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        mStatusTextPaint.setTextAlign(Paint.Align.CENTER);
        mStatusTextPaint.setTextSize(mStatusTextSize);
        mStatusTextPaint.setColor(mStatusTextColor);
    }

    @Override
    void onValueEntered(@NonNull String valueDigits) {
        //Do nothing
    }

    @Override
    public void onFingerprintAuthSuccess(FingerprintManager.CryptoObject cryptoObject) {
        onAuthenticationSuccess();
    }

    @Override
    public void onFingerprintAuthFailed(int errorCode, String errorMessage) {
        switch (errorCode) {
            case FingerPrintAuthHelper.CANNOT_RECOGNIZE_ERROR:
            case FingerPrintAuthHelper.NON_RECOVERABLE_ERROR:
            case FingerPrintAuthHelper.RECOVERABLE_ERROR:
                mStatusTextPaint.setColor(Color.RED);
                mCurrentStatusText = errorMessage;
                onAuthenticationFail();
                break;
        }
    }

    private void playErrorAnimation() {
        ValueAnimator goLeftAnimator = ValueAnimator.ofInt(0, 10);
        goLeftAnimator.setDuration(500);
        goLeftAnimator.setInterpolator(new CycleInterpolator(2));
        goLeftAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mBounds.left += (int) animation.getAnimatedValue();
                mBounds.right += (int) animation.getAnimatedValue();
                getRootView().invalidate();
            }
        });
        goLeftAnimator.start();

        goLeftAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                new android.os.Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCurrentStatusText = mNormalStatusText;
                        mStatusTextPaint.setColor(mStatusTextColor);
                        getRootView().invalidate();
                    }
                }, 1000);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    ///////////////// SETTERS/GETTERS //////////////

    @NonNull
    String getStatusText() {
        return mNormalStatusText;
    }

    void setStatusText(@NonNull String statusText) {
        this.mNormalStatusText = statusText;
        mCurrentStatusText = mNormalStatusText;
    }

    int getStatusTextColor() {
        return mStatusTextColor;
    }

    void setStatusTextColor(@ColorInt int statusTextColor) {
        this.mStatusTextColor = statusTextColor;
    }

    float getStatusTextSize() {
        return mStatusTextSize;
    }

    void setStatusTextSize(float statusTextSize) {
        this.mStatusTextSize = statusTextSize;
    }

    Boolean setFingerPrintEnable() {
        return isFingerPrintBoxVisible;
    }

    void setFingerPrintEnable(boolean isEnable) {
        this.isFingerPrintBoxVisible = isEnable && FingerPrintUtils.isFingerPrintEnrolled(getContext());
    }
}
