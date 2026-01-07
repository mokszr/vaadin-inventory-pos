import Chart from "chart.js/auto";

const registry = new WeakMap();

function ensureCanvas(root) {
  let canvas = root.querySelector("canvas");
  if (!canvas) {
    canvas = document.createElement("canvas");
    canvas.style.width = "100%";
    canvas.style.height = "280px";
    root.appendChild(canvas);
  }
  return canvas;
}

function destroyIfExists(root) {
  const old = registry.get(root);
  if (old) old.destroy();
  registry.delete(root);
}

function render(root, type, labels, datasetLabel, values) {
  if (!root) return;

  const canvas = ensureCanvas(root);
  destroyIfExists(root);

  const ctx = canvas.getContext("2d");

  const chart = new Chart(ctx, {
    type: type, // "line" or "bar"
    data: {
      labels: labels || [],
      datasets: [{
        label: datasetLabel || "",
        data: values || []
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: true }
      },
      scales: {
        y: { beginAtZero: true }
      }
    }
  });

  registry.set(root, chart);
}

window.DashboardCharts = { render };
