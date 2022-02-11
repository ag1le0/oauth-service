
//upload data
var response;
var table;
var test;

$('#file').on('change', function () {
	var file_data = $('#file').prop('files')[0];

	var form = new FormData();
	form.append('file', file_data);

	for(var p of form) {
		console.log(p);
	}

	var settings = {
	  "async": true,
	  "crossDomain": true,
	  "url": "/hr-system/api/hr/salary",
	  "method": "POST",
	  "processData": false,
	  "contentType": false,
	  "mimeType": "multipart/form-data",
	  "data": form
	}
    $.ajax({
        type: "POST",
        url: "/hr-system/api/hr/salary",
        data: form,
        dataType : "json",
        processData: false,
        contentType: false,
        mimeType: "multipart/form-data",
        success: function(data){
            response = data;
            console.log(response);
            $('#infoEmp').html("");
            for(i in response){
                var dem=parseInt(i)+1;
                var addInfo='<tr data-id="'+response[i]['Employee ID']+'">'+
                    '<td>'+
                        '<a class="preview" id="view" onclick="previewClick(\''+response[i]['Employee ID']+'\')"><i class="fa fa-download"></i></a>'+
                        '<a class="send" id="sent" onclick="sendClick(\''+response[i]['Employee ID']+'\')" data-id="'+response[i]['Employee ID']+'"><i class="fa fa-paper-plane"></i></a>'+
                    '</td>'+
                    '<td>'+dem+'</td>'+
                    '<td>'+response[i].FG+'</td>'+
                    '<td>'+response[i].Branch+'</td>'+
                    '<td>'+response[i].Part+'</td>'+
                    '<td>'+response[i].Code+'</td>'+
                    '<td>'+response[i].Name+'</td>'+
                    '<td>'+response[i]['Employee ID']+'</td>'+
                    '<td>'+response[i].Level+'</td>'+
                    '<td>'+response[i].Title+'</td>'+
                    '<td>'+response[i].USD2VND+'</td>'+
                    '<td>'+response[i].Join+'</td>'+
                    '<td>'+response[i]['Day of Month']+'</td>'+
                    '<td>'+response[i]["Basic Salary"]+'</td>'+
                    '<td>'+response[i]["Subtraction"]+'</td>'+
                    '<td>'+response[i]["Work Days"]+'</td>'+
                    '<td>'+response[i].Data[0]+'</td>'+
                    '<td>'+response[i].Data[1]+'</td>'+
                    '<td>'+response[i].Data[2]+'</td>'+
                    '<td>'+response[i].Data[3]+'</td>'+
                    '<td>'+response[i].Data[4]+'</td>'+
                    '<td>'+response[i].Data[5]+'</td>'+
                    '<td>'+response[i].Data[6]+'</td>'+
                    '<td>'+response[i].Data[7]+'</td>'+
                    '<td>'+response[i].Data[8]+'</td>'+
                    '<td>'+response[i].Data[9]+'</td>'+
                    '<td>'+response[i].Data[10]+'</td>'+
                    '<td>'+response[i].Data[11]+'</td>'+
                    '<td>'+response[i].Data[12]+'</td>'+

                '</tr>';
                $('#infoEmp').append(addInfo);
            }
            $(document).ready(function() {
                table = $('#wrapper').DataTable();
            } );

        },
        failure: function(errMsg) {
            console.log(errMsg);
            alert("Confirm failed!");
        }
    });
});


//Phân trang

//preview
function previewClick(employeeId) {
    window.location.href="/hr-system/api/hr/salary/export?employeeId="+employeeId;
    document.getElementById("view").style.color="red";

}
//send

function sendClick(employeeId)
{
    var meg=confirm("Do you want to send?");
    if(meg==true)
    {
        $.ajax({
            type: "POST",
            url: "/hr-system/api/hr/send",
            data: {"employeeId":employeeId},
            success: function(data){
            },
            failure: function(errMsg) {
                console.log(errMsg);
                alert("Confirm failed!");
            }
        });
        document.getElementById("sent").style.color="red";
    }

}

function send(employeeId){
    $.ajax({
        type: "POST",
        url: "/hr-system/api/hr/send",
        data: {"employeeId":employeeId},
        success: function(data){
        },
        failure: function(errMsg) {
            console.log(errMsg);
            alert("Confirm failed!");
        }
    });
}

//send all
function sendAll()
{
    var meg=confirm("Do you want to send all?");
    if(meg==true)
    {
        var data = table.rows().data();
        data.each(function(value, index){
            console.log(value[7]);
            send(value[7]);
        });
    }
}
