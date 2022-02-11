/* ------------------------------------------------------------------------------
*
*  # Date and time pickers
*
*  Specific JS code additions for picker_date.html page
*
*  Version: 1.0
*  Latest update: Aug 1, 2015
*
* ---------------------------------------------------------------------------- */

$(function() {

    // single date picker
    /*$('.daterange-single').daterangepicker({
        singleDatePicker: true,
        opens: "right",
        applyClass: 'bg-slate-600',
        cancelClass: 'btn-default',
        locale: {
            format: 'YYYY/MM/DD'
        }
    });

    // range date time 24 picker
    $('.datetimerange[side=right]').daterangepicker({
        maxSpan: {
            days: 7
        },
        timePicker: true,
        timePicker24Hour: true,
        opens: "right",
        applyClass: 'bg-slate-600',
        cancelClass: 'btn-default',
        timePickerIncrement: 30,
        locale: {
            format: 'YYYY/MM/DD HH:mm'
        }
    });

    $('.datetimerange[side=left]').daterangepicker({
        maxSpan: {
            days: 7
        },
        timePicker: true,
        timePicker24Hour: true,
        opens: "left",
        applyClass: 'bg-slate-600',
        cancelClass: 'btn-default',
        timePickerIncrement: 30,
        locale: {
            format: 'YYYY/MM/DD HH:mm'
        }
    });*/

    $('.bootstrap-select').selectpicker();

    /*$(".file-input-overwrite").fileinput({
        previewFileType: 'image',
        browseLabel: '',
        browseIcon: '<i class="icon-image2 position-left"></i> ',
        removeLabel: '',
        removeIcon: '<i class="icon-cross3"></i>',
        layoutTemplates: {
            icon: '<i class="icon-file-check"></i>',
            main1: "{preview}\n" +
            "<div class='input-group {class}'>\n" +
            "   <div class='input-group-btn'>\n" +
            "       {browse}\n" +
            "   </div>\n" +
            "</div>"
        },
        initialPreview: [
            "<img src='assets/images/placeholder.jpg' class='file-preview-image' alt=''>",
        ],
        overwriteInitial: true
    });

    $('input.file-input').fileinput({
        previewFileType: 'image',
        browseLabel: '',
        browseIcon: '<i class="icon-image2 position-left"></i> ',
        removeLabel: '',
        removeIcon: '<i class="icon-cross3"></i>',
        layoutTemplates: {
            icon: '<i class="icon-file-check"></i>'
        }
    });*/

});
