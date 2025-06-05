function handleCanvasClick(event) {
    const canvas = event.currentTarget;
    const rect = canvas.getBoundingClientRect();
    const clickX = event.clientX - rect.left;
    const clickY = event.clientY - rect.top;
    const rInputElement = document.getElementById("r");
    const rValue = parseFloat(rInputElement.value);

    if (isNaN(rValue) || rValue <= 0) {
        alert("Невозможно определить координаты точки. Радиус R не установлен или некорректен.");
        return;
    }

    const canvasWidth = rect.width;
    const canvasHeight = rect.height;
    const plotRadius = 0.9 * (canvasWidth / 2);
    const scale = plotRadius / rValue;
    const x = (clickX - canvasWidth / 2) / scale;
    const y = (canvasHeight / 2 - clickY) / scale;
    const roundedX = Math.round(x * 10) / 10;
    const roundedY = Math.round(y * 10) / 10;

    if (!validateInput(roundedX, roundedY, rValue)) {
        alert("Пожалуйста, выберите допустимые координаты на графике.");
        return;
    }

    const xInput = document.getElementById("x");
    if (xInput) {
        xInput.value = roundedX;
    } else {
        alert("Internal error: Unable to set X coordinate.");
        return;
    }

    const yInput = document.getElementById("y");
    const yHiddenInput = document.getElementById("yHidden");
    if (yInput && yHiddenInput) {
        yInput.value = roundedY;
        yHiddenInput.value = roundedY;
        const eventChange = new Event('change', { bubbles: true });
        yInput.dispatchEvent(eventChange);
        yHiddenInput.dispatchEvent(eventChange);
    } else {
        alert("Internal error: Unable to set Y coordinate.");
        return;
    }

    const submitBtn = document.getElementById("submitBtn");
    if (submitBtn) {
        submitBtn.click();
    } else {
        alert("Internal error: Unable to submit the form.");
    }
}

document.addEventListener('DOMContentLoaded', function() {
    drawPlot();
});

function drawPlot() {
    const canvas = document.getElementById("plotCanvas");
    const ctx = canvas.getContext("2d");
    const width = canvas.width;
    const height = canvas.height;
    const scale = 5;
    const fontSize = 12 * scale;
    ctx.font = fontSize + "px Arial";
    const R = 0.9 * (width / 2);
    const pointSize = 5 * scale;
    ctx.clearRect(0, 0, width, height);

    ctx.fillStyle = "rgba(184, 80, 134, 0.8)";
    ctx.beginPath();
        ctx.moveTo(width / 2, height / 2);
        ctx.lineTo(width / 2 - R, height / 2);
        ctx.lineTo(width / 2, height / 2 + R);
        ctx.closePath();
        ctx.fill();


        ctx.beginPath();
        ctx.moveTo(width / 2, height / 2);
        ctx.lineTo(width / 2 + R, height / 2);
        ctx.lineTo(width / 2 + R, height / 2 + R / 2);
        ctx.lineTo(width / 2, height / 2 + R / 2);
        ctx.closePath();
        ctx.fill();


        ctx.beginPath();
        ctx.moveTo(width / 2, height / 2);
        ctx.arc(width / 2, height / 2, R / 2, Math.PI, 1.5 * Math.PI, false); // Arc from left to top
        ctx.closePath();
        ctx.fill();

    ctx.beginPath();
    ctx.moveTo(0, height / 2);
    ctx.lineTo(width, height / 2);
    ctx.lineWidth = 2;
    ctx.strokeStyle = "white";
    ctx.stroke();

    ctx.beginPath();
    ctx.moveTo(width / 2, 0);
    ctx.lineTo(width / 2, height);
    ctx.lineWidth = 2;
    ctx.strokeStyle = "white";
    ctx.stroke();

    ctx.beginPath();
    ctx.moveTo(width, height / 2);
    ctx.lineTo(width - 10 * scale, height / 2 - 5 * scale);
    ctx.lineTo(width - 10 * scale, height / 2 + 5 * scale);
    ctx.closePath();
    ctx.fillStyle = "white";
    ctx.fill();

    ctx.beginPath();
    ctx.moveTo(width / 2, 0);
    ctx.lineTo(width / 2 - 5 * scale, 10 * scale);
    ctx.lineTo(width / 2 + 5 * scale, 10 * scale);
    ctx.closePath();
    ctx.fillStyle = "white";
    ctx.fill();

    ctx.fillStyle = "white";
    ctx.fillText("X", width - fontSize, height / 2 - 10);
    ctx.fillText("Y", width / 2 + 10, fontSize);

    drawAxisMarks(ctx, width, height, R, fontSize);
    drawPoints(ctx, width, height, R, pointSize);
}

function drawAxisMarks(ctx, width, height, R, fontSize) {
    const marks = [-1, -0.5, 0.5, 1];
    ctx.font = fontSize + "px Arial";
    ctx.fillStyle = "white";

    marks.forEach(mark => {
        const position = mark * R;
        ctx.beginPath();
        ctx.moveTo(width / 2 + position, height / 2 - 5);
        ctx.lineTo(width / 2 + position, height / 2 + 5);
        ctx.stroke();
        const labelX = mark === 1 ? 'R' : mark === -1 ? '-R' : mark === 0.5 ? 'R/2' : '-R/2';
        ctx.fillText(labelX, width / 2 + position - 10, height / 2 + 20);

        ctx.beginPath();
        ctx.moveTo(width / 2 - 5, height / 2 - position);
        ctx.lineTo(width / 2 + 5, height / 2 - position);
        ctx.stroke();
        const labelY = mark === 1 ? 'R' : mark === -1 ? '-R' : mark === 0.5 ? 'R/2' : '-R/2';
        ctx.fillText(labelY, width / 2 + 10, height / 2 - position + 5);
    });
}

function drawPoints(ctx, width, height, canvasR, pointSize) {
    const rows = document.querySelectorAll("#resultsTable tbody tr");

    rows.forEach(row => {
        const cells = row.getElementsByTagName("td");
        const x = parseFloat(cells[1].innerText);
        const y = parseFloat(cells[2].innerText);
        const r = parseFloat(cells[3].innerText);
        const hitResult = cells[4].innerText.trim() === "Hit";

        if (!isNaN(x) && !isNaN(y) && !isNaN(r) && r !== 0) {
            const scale = canvasR / r;
            const canvasX = width / 2 + x * scale;
            const canvasY = height / 2 - y * scale;
            ctx.beginPath();
            ctx.arc(canvasX, canvasY, pointSize, 0, 2 * Math.PI);
            ctx.fillStyle = hitResult ? "lightblue" : "gray";
            ctx.fill();
        }
    });
}

function validateInput(x, y, r) {
    return Math.abs(x) <= r && Math.abs(y) <= r;
}

document.addEventListener('DOMContentLoaded', function() {
    drawPlot();
});
