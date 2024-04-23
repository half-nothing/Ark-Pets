/** Copyright (c) 2022-2024, Harry Huang
 * At GPL-3.0 License
 */
package cn.harryh.arkpets.utils;

import cn.harryh.arkpets.Const;
import javafx.animation.ScaleTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;

import java.lang.reflect.Method;
import java.util.List;


public class GuiComponents {
    /** A useful tool to initialize a {@link Slider} control.
     * @param <N> The value type of the Slider.
     */
    @SuppressWarnings("UnusedReturnValue")
    abstract public static class SliderSetup<N extends Number> {
        protected final Slider slider;
        protected Labeled display;
        protected final DoubleProperty proxy;
        protected final ChangeListener<? super Number> listener;
        protected ChangeListener<? super Number> listenerForDisplay;
        protected ChangeListener<? super Number> listenerForExternal;
        protected static final double initialValue = Double.MIN_VALUE;

        public SliderSetup(Slider slider) {
            this.slider = slider;
            // Initialize the property proxy.
            this.proxy = new DoublePropertyBase(initialValue) {
                @Override
                public Object getBean() {
                    return SliderSetup.this;
                }

                @Override
                public String getName() {
                    return "value";
                }
            };
            // Add a listener to the slider to bind the property proxy to it.
            listener = (observable, oldValue, newValue) -> {
                double validatedValue = getValidatedValue().doubleValue();
                if (validatedValue != getSliderValue())
                    setSliderValue(validatedValue);
                else
                    this.proxy.setValue(validatedValue);
            };
            slider.valueProperty().addListener(listener);
        }

        abstract protected N adjustValue(double rawValue);

        public final SliderSetup<N> setDisplay(Labeled display, String format, String tooltipText) {
            this.display = display;
            // Initialize the tooltip for the display node.
            Tooltip tooltip = new Tooltip(tooltipText);
            tooltip.setStyle(GuiPrefabs.tooltipStyle);
            display.setTooltip(tooltip);
            // Add the listener to update the display's text.
            if (listenerForDisplay != null)
                proxy.removeListener(listenerForDisplay);
            listenerForDisplay = (observable, oldValue, newValue) ->
                    display.setText(String.format(format, getValidatedValue()));
            proxy.addListener(listenerForDisplay);
            return this;
        }

        public final SliderSetup<N> setOnChanged(ChangeListener<? super Number> handler) {
            if (listenerForExternal != null)
                proxy.removeListener(listenerForExternal);
            listenerForExternal = handler;
            if (listenerForExternal != null)
                proxy.addListener(listenerForExternal);
            return this;
        }

        public final SliderSetup<N> setRange(N min, N max) {
            slider.setMin(min.doubleValue());
            slider.setMax(max.doubleValue());
            return this;
        }

        public final SliderSetup<N> setTicks(N majorTickUnit, int minorTickCount) {
            slider.setMajorTickUnit(majorTickUnit.doubleValue());
            slider.setMinorTickCount(minorTickCount);
            return this;
        }

        public final SliderSetup<N> setSliderValue(double newValue) {
            slider.setValue(newValue);
            return this;
        }

        public final double getSliderValue() {
            return slider.getValue();
        }

        public final N getValidatedValue() {
            return adjustValue(getSliderValue());
        }
    }


    /** A useful tool to initialize a {@link ComboBox} control whose items are numeric values.
     * @param <N> The value type of the items.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static class ComboBoxSetup<N extends Number> {
        protected final ComboBox<NamedItem<N>> comboBox;

        public ComboBoxSetup(ComboBox<NamedItem<N>> comboBox) {
            this.comboBox = comboBox;
        }

        @SafeVarargs
        public final ComboBoxSetup<N> setItems(NamedItem<N>... elements) {
            comboBox.getItems().setAll(elements);
            return this;
        }

        public final ComboBoxSetup<N> selectValue(N targetValue, String defaultName) {
            if (NamedItem.match(comboBox.getItems(), targetValue) == null)
                comboBox.getItems().add(new NamedItem<>(defaultName, targetValue));
            comboBox.getSelectionModel().select(NamedItem.match(comboBox.getItems(), targetValue));
            return this;
        }

        public final ComboBoxSetup<N> setOnNonNullValueUpdated(ChangeListener<NamedItem<N>> listener) {
            comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null)
                    listener.changed(observable, oldValue, newValue);
            });
            return this;
        }
    }


    public static final class SimpleIntegerSliderSetup extends SliderSetup<Integer> {
        public SimpleIntegerSliderSetup(Slider slider) {
            super(slider);
        }

        @Override
        protected Integer adjustValue(double rawValue) {
            return Math.toIntExact(Math.round(rawValue));
        }
    }


    public static final class SimpleMultipleIntegerSliderSetup extends SliderSetup<Integer> {
        private final float commonMultiple;

        public SimpleMultipleIntegerSliderSetup(Slider slider, float commonMultiple) {
            super(slider);
            this.commonMultiple = commonMultiple;
        }

        @Override
        protected Integer adjustValue(double rawValue) {
            return Math.toIntExact(Math.round(Math.round(rawValue / commonMultiple) * commonMultiple));
        }
    }


    public record NamedItem<N extends Number>(String name, N value) {
        @Override
        public String toString() {
            return name;
        }

        public static <N extends Number> NamedItem<N> match(List<NamedItem<N>> candidateOptions, N targetValue) {
            for (NamedItem<N> i : candidateOptions)
                if (targetValue.equals(i.value))
                    return i;
            return null;
        }
    }


    abstract public static class NoticeBar {
        protected static final double borderRadius = 8;
        protected static final double internalSpacing = 8;
        protected static final double iconScale = 0.75;
        protected static final double inserts = 4;
        protected static final double widthScale = 0.85;
        protected final Pane container;
        protected Pane noticeBar;

        public NoticeBar(Pane root) {
            container = root;
        }

        public final void refresh() {
            if (isToActivate())
                activate();
            else
                suppress();
        }

        public final void activate() {
            if (noticeBar == null) {
                noticeBar = getNoticeBar(getWidth(), getHeight());
                container.getChildren().add(noticeBar);
                ScaleTransition transition = new ScaleTransition(Const.durationFast, noticeBar);
                transition.setFromY(0.1);
                transition.setToY(1);
                transition.play();
            }
        }

        public final void suppress() {
            if (noticeBar != null) {
                final Pane finalNoticeBar = noticeBar;
                noticeBar = null;
                ScaleTransition transition = new ScaleTransition(Const.durationFast, finalNoticeBar);
                transition.setFromY(1);
                transition.setToY(0.1);
                transition.setOnFinished(e -> container.getChildren().remove(finalNoticeBar));
                transition.play();
            }
        }

        abstract protected String getColorString();

        abstract protected String getIconSVGPath();

        abstract protected String getText();

        protected boolean isToActivate() {
            throw new UnsupportedOperationException("Unimplemented method invoked");
        }

        protected double getHeight() {
            return Font.getDefault().getSize() * 2;
        }

        protected double getWidth() {
            Region region = (Region)container.getParent();
            double regionWidth = region.getWidth() - region.getInsets().getLeft() - region.getInsets().getRight();
            return regionWidth * widthScale;
        }

        protected Pane getNoticeBar(double width, double height) {
            // Colors
            Color color = Color.valueOf(getColorString());
            BackgroundFill bgFill = new BackgroundFill(
                    color.deriveColor(0, 0.5, 1.5, 0.25),
                    new CornerRadii(borderRadius),
                    new Insets(inserts)
            );
            // Layouts
            HBox bar = new HBox(internalSpacing);
            bar.setBackground(new Background(bgFill));
            bar.setMinSize(width, height);
            bar.setAlignment(Pos.CENTER_LEFT);
            SVGPath icon = new SVGPath();
            icon.setContent(getIconSVGPath());
            icon.setFill(color);
            icon.setScaleX(iconScale);
            icon.setScaleY(iconScale);
            icon.setTranslateX(inserts);
            Label label = new Label(getText());
            label.setMinWidth(width * widthScale * widthScale);
            label.setPadding(new Insets(inserts));
            label.setTextFill(color);
            label.setWrapText(true);
            bar.getChildren().addAll(icon, label);
            // Click event
            try {
                Method onClick = getClass().getDeclaredMethod("onClick", MouseEvent.class);
                if (!NoticeBar.class.equals(onClick.getDeclaringClass())) {
                    // If the method "onClick" has been overridden:
                    bar.setCursor(Cursor.HAND);
                    bar.setOnMouseClicked(this::onClick);
                }
            } catch (Exception ignored) {
            }
            return bar;
        }

        protected void onClick(MouseEvent event) {
        }
    }


    abstract public static class Handbook {
        public boolean hasShown = false;

        public Handbook() {
        }

        abstract public String getTitle();

        abstract public String getHeader();

        abstract public String getContent();

        public SVGPath getIcon() {
            return GuiPrefabs.Icons.getIcon(GuiPrefabs.Icons.ICON_HELP_ALT, GuiPrefabs.Colors.COLOR_INFO);
        }

        public boolean hasShown() {
            return hasShown;
        }

        public void setShown() {
            hasShown = true;
        }
    }
}
