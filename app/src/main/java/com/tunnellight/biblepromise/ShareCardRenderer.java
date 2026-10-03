package com.tunnellight.biblepromise;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Draws the current verse over its background photo into a portrait bitmap,
 * styled like the main screen, for sharing as an image.
 */
final class ShareCardRenderer {

    /** Output image size in pixels (9:16, like a phone screen). */
    static final int WIDTH_PX = 1080;
    static final int HEIGHT_PX = 1920;

    /** Size the card is laid out at, in dp, so text scales like on a phone. */
    private static final float LAYOUT_WIDTH_DP = 411f;

    /** Smallest the verse text is shrunk to when a long verse doesn't fit. */
    private static final float MIN_VERSE_PX_RATIO = 0.5f;

    private ShareCardRenderer() {
    }

    /**
     * Renders the card. Text content, sizes and typefaces are copied from the
     * on-screen views so the image matches what the user sees.
     */
    static Bitmap render(Context context, int backgroundRes, TextView verse,
                         TextView reference, TextView quote, TextView attribution) {
        // The card is never attached; a throwaway parent just resolves its layout params.
        View card = LayoutInflater.from(context)
                .inflate(R.layout.share_card, new FrameLayout(context), false);

        ((ImageView) card.findViewById(R.id.cardBackground)).setImageResource(backgroundRes);
        TextView cardVerse = card.findViewById(R.id.cardVerseText);
        copyText(verse, cardVerse);
        copyText(reference, card.findViewById(R.id.cardReference));
        copyText(quote, card.findViewById(R.id.cardQuote));
        ((TextView) card.findViewById(R.id.cardAttribution)).setText(attribution.getText());

        float density = context.getResources().getDisplayMetrics().density;
        int layoutWidth = Math.round(LAYOUT_WIDTH_DP * density);
        int layoutHeight = Math.round(layoutWidth * (HEIGHT_PX / (float) WIDTH_PX));
        measure(card, layoutWidth, layoutHeight);

        // Shrink very long verses until the verse block fits.
        LinearLayout block = card.findViewById(R.id.cardVerseBlock);
        float minSize = cardVerse.getTextSize() * MIN_VERSE_PX_RATIO;
        while (contentHeight(block) > block.getMeasuredHeight()
                && cardVerse.getTextSize() * 0.9f >= minSize) {
            cardVerse.setTextSize(TypedValue.COMPLEX_UNIT_PX, cardVerse.getTextSize() * 0.9f);
            measure(card, layoutWidth, layoutHeight);
        }
        // contentHeight() re-measures children, so measure the whole card once more.
        measure(card, layoutWidth, layoutHeight);
        card.layout(0, 0, layoutWidth, layoutHeight);

        Bitmap bitmap = Bitmap.createBitmap(WIDTH_PX, HEIGHT_PX, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(WIDTH_PX / (float) layoutWidth, HEIGHT_PX / (float) layoutHeight);
        card.draw(canvas);
        return bitmap;
    }

    private static void copyText(TextView from, TextView to) {
        to.setText(from.getText());
        to.setTypeface(from.getTypeface());
        to.setTextSize(TypedValue.COMPLEX_UNIT_PX, from.getTextSize());
    }

    private static void measure(View view, int width, int height) {
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY));
    }

    /** Total height the block's children want, including their margins. */
    private static int contentHeight(LinearLayout block) {
        int total = block.getPaddingTop() + block.getPaddingBottom();
        for (int i = 0; i < block.getChildCount(); i++) {
            View child = block.getChildAt(i);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) child.getLayoutParams();
            child.measure(
                    View.MeasureSpec.makeMeasureSpec(
                            block.getMeasuredWidth() - lp.leftMargin - lp.rightMargin,
                            lp.width == LinearLayout.LayoutParams.MATCH_PARENT
                                    ? View.MeasureSpec.EXACTLY : View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            total += child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
        }
        return total;
    }
}
