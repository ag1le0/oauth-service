function loadModels(dataset, flag, onLoad) {
    $.ajax({
        type: 'GET',
        url: '/api/test/model',
        data: {
            factory: dataset.factory,
            parameter: dataset.parameter,
            timeSpan: dataset.timeSpan
        },
        success: function(data){
            var selector = $("select[name='modelName']");
            selector.children('option').remove();

            var options = "";
            for(i in data){
                options+='<option value=' + data[i].modelName + '>' + data[i].modelName + '</option>';
            }
            selector.append(options);

            if (data.length > 0) {
                if (flag == undefined || flag == false) {
                    dataset.modelName = data[0].modelName;
                }
                selector.val(dataset.modelName);
                onLoad(dataset, flag);
            }

            selector.selectpicker('refresh');
        },
        failure: function(errMsg) {
             console.log(errMsg);
        },
   });
}

function loadGroups(dataset, flag, onLoad) {
    $.ajax({
        type: 'GET',
        url: '/api/test/group',
        data: {
            factory: dataset.factory,
            modelName: dataset.modelName,
            parameter: dataset.parameter,
            timeSpan: dataset.timeSpan
        },
        success: function(data){
            var selector = $("select[name='groupName']");
            selector.children('option').remove();

            var options = "";
            for(i in data){
                options+='<option value=' + data[i].groupName + '>' + data[i].groupName + '</option>';
            }
            selector.append(options);

            if (data.length > 0) {
                if (flag == undefined || flag == false) {
                    dataset.groupName = data[0].groupName;
                }
                selector.val(dataset.groupName);
                onLoad(dataset, flag);
            }

            selector.selectpicker('refresh');
        },
        failure: function(errMsg) {
             console.log(errMsg);
        },
   });
}

function loadStations(dataset, flag, onLoad, all) {
    $.ajax({
        type: 'GET',
        url: '/api/test/station',
        data: {
            factory: dataset.factory,
            modelName: dataset.modelName,
            groupName: dataset.groupName,
            parameter: dataset.parameter,
            timeSpan: dataset.timeSpan
        },
        success: function(data){
            var selector = $("select[name='stationName']");
            selector.children('option').remove();

            var options = "";
            if (all != undefined && all == true) {
                options+='<option value="">ALL</option>';
            }
            for(i in data){
                options+='<option value=' + data[i].stationName + '>' + data[i].stationName + '</option>';
            }
            selector.append(options);

            if (data.length > 0) {
                if (flag == undefined || flag == false) {
                    if (all != undefined && all == true) {
                        dataset.stationName = "";
                    } else {
                        dataset.stationName = data[0].stationName;
                    }
                }
                selector.val(dataset.stationName);
                onLoad(dataset, flag);
            }

            selector.selectpicker('refresh');
        },
        failure: function(errMsg) {
             console.log(errMsg);
        },
   });
}

function loadParameters(dataset, flag, onLoad) {
    $.ajax({
        type: 'GET',
        url: '/api/test/model/parameter',
        data: {
            factory: dataset.factory,
            modelName: dataset.modelName
        },
        success: function(data){
            var selector = $("select[name='parameter']");
            selector.children('option').remove();

            var options = "";
            for(i in data){
                options+='<option value="' + data[i].parameters + '" data-low=' + data[i].lowSpec + ' data-high=' + data[i].highSpec + '>' + data[i].parameters + '</option>';
            }
            selector.append(options);
            selector.selectpicker('refresh');

            onLoad(dataset, flag);
        },
        failure: function(errMsg) {
             console.log(errMsg);
        },
   });
}

function getUrl(type, factory, modelName, groupName, stationName) {
    var url = "http://10.224.81.70:8888";

    // add path
    switch (type) {
        case "LOCKED_A":
        case "LOCKED_B":
        case "LOCKED_TIMEOUT":
            url += "/troubleshooting-guide";
            break;
        case "WARNING_CPK":
            url += "/station-cpk";
            break;
        default:
            url += "/station-detail";
    }

    // add params
    url = url + "?factory=" + factory + "&modelName=" + modelName + "&groupName=" + groupName + "&stationName=" + stationName;

    return url;
}

function getTimeSpan(days) {
    if (days == undefined) {
        days = 0;
    }

    var timeSpan = {
        shiftType: "",
        startDate: new Date(),
        endDate: new Date()
    }

    var day = new Date();
    day.setHours(7, 30, 0, 0);

    var night = new Date();
    night.setHours(19, 30, 0, 0);

    var now = new Date();

    if (now - day < 0) {
        timeSpan.shiftType = "NIGHT";
        timeSpan.startDate.setDate(timeSpan.startDate.getDate() - 1 - days);
        timeSpan.startDate.setHours(7, 30);
        timeSpan.endDate.setHours(7, 30);
        return timeSpan;
    } else if (now - night < 0) {
        timeSpan.shiftType = "DAY";
        timeSpan.startDate.setDate(timeSpan.startDate.getDate() - days);
        timeSpan.startDate.setHours(7, 30);
        timeSpan.endDate.setDate(timeSpan.endDate.getDate() + 1);
        timeSpan.endDate.setHours(7, 30);
        return timeSpan;
    } else {
        timeSpan.shiftType = "NIGHT";
        timeSpan.startDate.setDate(timeSpan.startDate.getDate() - days);
        timeSpan.startDate.setHours(7, 30);
        timeSpan.endDate.setDate(timeSpan.endDate.getDate() + 1);
        timeSpan.endDate.setHours(7, 30);
        return timeSpan;
    }
}

function getOldTimeSpan(days) {
    if (days == undefined) {
        days = 0;
    }

    var timeSpan = {
        shiftType: "",
        startDate: new Date(),
        endDate: new Date()
    }

    var day = new Date();
    day.setHours(7, 30, 0, 0);

    var night = new Date();
    night.setHours(19, 30, 0, 0);

    var now = new Date();

    if (now - day < 0) {
        timeSpan.shiftType = "NIGHT";
        timeSpan.startDate.setDate(timeSpan.startDate.getDate() - 1 - days);
        timeSpan.startDate.setHours(19, 30);
        timeSpan.endDate.setHours(7, 30);
        return timeSpan;
    } else if (now - night < 0) {
        timeSpan.shiftType = "DAY";
        timeSpan.startDate.setDate(timeSpan.startDate.getDate() - days);
        timeSpan.startDate.setHours(7, 30);
        timeSpan.endDate.setHours(19, 30);
        return timeSpan;
    } else {
        timeSpan.shiftType = "NIGHT";
        timeSpan.startDate.setDate(timeSpan.startDate.getDate() - days);
        timeSpan.startDate.setHours(19, 30);
        timeSpan.endDate.setDate(timeSpan.endDate.getDate() + 1);
        timeSpan.endDate.setHours(7, 30);
        return timeSpan;
    }
}

function activeMenu(path) {
    $('.imenu[path="'+ path +'"]').addClass('active');
}

function openVLC(url) {
    if( /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent) ) {
        window.location.href = 'intent://' + url + '#Intent;scheme=rtsp;package=org.videolan.vlc;end';
    } else {
        window.location.href = 'vlc://' + url;
    }
}

function openSingleVLC(url) {
    if( /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent) ) {
        window.location.href = 'intent://' + url + '#Intent;scheme=rtsp;package=org.videolan.vlc;end';
    } else {
        window.location.href = 'vlc://rtsp://' + url;
    }
}

function hrefWindow(href) {
    window.location.href = href;
}

function openWindow(href) {
    window.open(href);
}

function toString(value) {
    return value != null ? value : '-';
}

function convertStatus(rate, errorSpec, warningSpec) {
    if (rate < errorSpec) {
        return 'bg-red';
    }
    if (rate < warningSpec) {
        return 'bg-yellow';
    }
    return 'bg-green';
}