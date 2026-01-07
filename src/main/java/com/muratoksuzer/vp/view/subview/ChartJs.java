package com.muratoksuzer.vp.view.subview;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.html.Div;

import java.util.List;

@Tag("div")
@JsModule("./charts/dashboard-charts.js")
@NpmPackage(value = "chart.js", version = "4.4.1")
public class ChartJs extends Div {

    public enum Type { LINE, BAR }

    private final Type type;
    private String[] labels = new String[0];
    private String datasetLabel = "";
    private Number[] values = new Number[0];

    public ChartJs(Type type) {
        this.type = type;
        setWidthFull();
        getStyle()
                .setBorder("1px solid var(--lumo-contrast-10pct)")
                .setBorderRadius("12px")
                .setPadding("12px")
                .setBoxShadow("var(--lumo-box-shadow-xs)");
    }

    public void setSeries(List<String> labels, String datasetLabel, List<? extends Number> values) {
        this.labels = labels.toArray(new String[0]);
        this.datasetLabel = datasetLabel;
        this.values = values.toArray(new Number[0]);
        render();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        render();
    }

    private void render() {
        getElement().executeJs(
                "window.DashboardCharts && window.DashboardCharts.render($0, $1, $2, $3, $4);",
                getElement(),
                type.name().toLowerCase(),
                labels,
                datasetLabel,
                values
        );

    }
}
