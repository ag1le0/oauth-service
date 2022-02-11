<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page trimDirectiveWhitespaces="true"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Smart Factory Management - FII by Icarus</title>

    <!-- Global stylesheets -->
    <link rel="stylesheet" type="text/css" href="/assets/css/bootstrap.css" >
    <link rel="stylesheet" type="text/css" href="/assets/css/core.css" >
    <link rel="stylesheet" type="text/css" href="/assets/css/components.css" >
    <link rel="stylesheet" type="text/css" href="/assets/css/colors.css" >
    <link rel="stylesheet" type="text/css" href="/assets/css/icons/icomoon/styles.css" >
    <link rel="stylesheet" type="text/css" href="/assets/css/icons/fontawesome/styles.min.css" >
    <!-- /global stylesheets -->

    <!-- Core JS files -->
    <script type="text/javascript" src="/assets/js/core/libraries/jquery.min.js"></script>
    <script type="text/javascript" src="/assets/js/core/libraries/bootstrap.min.js"></script>
    <script type="text/javascript" src="/assets/js/core/app.js"></script>
    <script type="text/javascript" src="/assets/js/plugins/loaders/pace.min.js"></script>
    <script type="text/javascript" src="/assets/js/plugins/loaders/blockui.min.js"></script>
    <script type="text/javascript" src="/assets/js/plugins/uploaders/fileinput.min.js"></script>
    <script type="text/javascript" src="/assets/js/plugins/ui/ripple.min.js"></script>
    <script type="text/javascript" src="/assets/js/plugins/forms/selects/bootstrap_select.min.js"></script>
    <script type="text/javascript" src="/assets/js/plugins/forms/tags/tagsinput.min.js"></script>
    <!-- /core JS files -->

    <!-- highchart -->
    <script type="text/javascript" src="/assets/js/plugins/charts/highchart/highcharts.js"></script>
    <script type="text/javascript" src="/assets/js/plugins/charts/highchart/modules/histogram-bellcurve.js"></script>
    <script type="text/javascript" src="/assets/js/plugins/charts/highchart/modules/series-label.js"></script>
    <script type="text/javascript" src="/assets/js/plugins/charts/highchart/modules/drilldown.js"></script>
    <script type="text/javascript" src="/assets/js/plugins/charts/highchart/modules/exporting.js"></script>
    <script type="text/javascript" src="/assets/js/plugins/charts/highchart/modules/export-data.js"></script>
    <script type="text/javascript" src="/assets/js/plugins/charts/highchart/modules/offline-exporting.js"></script>
    <script type="text/javascript" src="/assets/js/plugins/charts/highchart/themes/grid-light.js"></script>
    <script type="text/javascript" src="/assets/js/plugins/charts/highchart/plugins/grouped-categories.js"></script>
    <!-- /highchart -->

    <!-- /daterangepicker -->
    <link rel="stylesheet" type="text/css" href="/assets/css/plugins/pickers/daterangepicker.css" >
    <script type="text/javascript" src="/assets/js/plugins/ui/moment/moment.min.js"></script>
    <script type="text/javascript" src="/assets/js/plugins/pickers/daterangepicker.min.js"></script>
    <!-- daterangepicker -->

    <script type="text/javascript" src="/assets/js/plugins/notify/bootstrap-notify.min.js"></script>

    <link rel="stylesheet" type="text/css" href="/assets/css/custom/slider_home.css">
    <script type="text/javascript" src="/assets/js/core/libraries/jssor.slider-27.5.0.min.js"></script>

    <!-- custom -->
    <script type="text/javascript" src="/assets/js/mobile/icivet.min.js"></script>
    <script type="text/javascript" src="/assets/js/custom/picker_date.js"></script>
    <script type="text/javascript" src="/assets/js/custom/chart.js"></script>

    <link rel="stylesheet" href="/assets/css/custom/style.css">
    <!-- custom -->
    

</head>

<body class="navbar-top sidebar-xs">

	<%@ include file="common/navbar.jsp" %>

    <!-- Page container -->
    <div class="page-container">

        <!-- Page content -->
        <div class="page-content">

            <%@ include file="common/sidebar.jsp" %>

            <!-- Main content -->
            <div class="content-wrapper" style="background-color: #272727;">

                <!-- Content area -->
                <div class="content" style="padding:3px 2px 60px 3px; background-color: #272727;">
                    <div>
                        <div class="col-lg-12" style="background-color: #272727;">
                            <%@ include file="router.jsp" %>
                        </div>
                    </div>

                    <%@ include file="common/footer.jsp" %>

                </div>
                <!-- /content area -->

            </div>
            <!-- /main content -->

        </div>
        <!-- /page content -->

    </div>
    <!-- /page container -->

</body>
</html>
