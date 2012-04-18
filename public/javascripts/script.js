$(document).ready(function () {


});

//resizable, selectable, sortable, slider, tabs

function calculateHumanReadableDateDiff(millisecondsToDate) {
    var millisecondsPerSecond = 1000;
    var secondsPerMinute = 60;
    var MinutesPerHour = 60;
    var hoursPerDay = 24;
    var count = millisecondsToDate;
    var diffContainer = new Object();
    miliseconds = count % millisecondsPerSecond;
    count = Math.floor(count / millisecondsPerSecond);
    diffContainer["seconds"] = count % secondsPerMinute;
    count = Math.floor(count / secondsPerMinute);
    diffContainer["minutes"] = count % MinutesPerHour;
    count = Math.floor(count / MinutesPerHour);
    diffContainer["hours"] = count % hoursPerDay;
    count = Math.floor(count / hoursPerDay);
    diffContainer["days"] = count;
    return diffContainer;
}

/** creates a count down timer that only supports minutes and seconds */
function countDownTimer(/* string */ jQuerySelector, /* date */ toDate, /* callback */ finished) {
    //timer that quits itself
    var hours = $(jQuerySelector + "_hours");
    var minutes = $(jQuerySelector + "_minutes");
    var seconds = $(jQuerySelector + "_seconds");

    var timer;
    var calculation = function () {
        var millisecondsToDate = toDate - new Date();
        if (millisecondsToDate > 0) {
            var diffContainer = calculateHumanReadableDateDiff(millisecondsToDate);
            hours.html(diffContainer["hours"]);
            var minutesFormatted = diffContainer["minutes"] < 10 ? "0" + diffContainer["minutes"] : diffContainer["minutes"];
            minutes.html(minutesFormatted);
            var secondsFormatted = diffContainer["seconds"] < 10 ? "0" + diffContainer["seconds"] : diffContainer["seconds"];
            seconds.html(secondsFormatted);
        } else {
            if (typeof finished == "function") {
                finished();
            }
            window.clearInterval(timer);
        }
    }
    timer = window.setInterval(calculation, 100);
}

function calcTimeNextDrawing(gameInfo, /* Date, default: new Date() */ now) {
    var now = now || new Date();
    var timeFirstDrawing = new Date(gameInfo.firstDrawing);
    var timeStampNextDrawing;
    var firstNumberHasNotDrawnYet = (timeFirstDrawing - now) > 0;
    if (firstNumberHasNotDrawnYet) {
        timeStampNextDrawing = timeFirstDrawing;
    } else {
        var millisecondsPerSecond = 1000;
        var difference = now - timeFirstDrawing;
        var drawing = Math.ceil(difference / (gameInfo.drawingIntervalInSeconds * millisecondsPerSecond));
        timeStampNextDrawing = timeFirstDrawing.getTime() + drawing * gameInfo.drawingIntervalInSeconds * millisecondsPerSecond;
    }
    var date = new Date(timeStampNextDrawing);
    return date;
}

function announceNumbersOnTable(announcedNumbersArray) {
    jQuery.each(announcedNumbersArray, function (index, value) {
        $("#announced_number_" + value).addClass('ui-selected');
    });
}

function announceCurrentNumber(jQuerySelectorString, announcedNumbersArray) {
    $(jQuerySelectorString).text(announcedNumbersArray[announcedNumbersArray.length - 1]);
}

function showMessage(/* string */ message, /*string */ type /* error, success*/) {
    $('#flashMessages').append('<div class="alert-message fade in ' + type + ' data-alert" data-alert="alert"><a class="close" href="#">×</a><p>' + message + '</p></div>');
}

function showError(message) {
    showMessage(message, 'error');
}

function showSuccess(message) {
    showMessage(message, 'success');
}
