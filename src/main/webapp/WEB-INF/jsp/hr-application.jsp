<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page trimDirectiveWhitespaces="true"%>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1, user-scalable=no">
    <title>Bootstrap</title>
    <link rel="stylesheet" type="text/css" href="/hr-system/assets/css/icons/fontawesome/styles.min.css" >
    <link rel="stylesheet" type="text/css" href="/hr-system/assets/css/bootstrap.css" >
    <link rel="stylesheet" type="text/css" href="/hr-system/assets/datatables/css/jquery.dataTables.min.css">
    <link rel="stylesheet" type="text/css" href="/hr-system/assets/css/custom/hr.css">

    <link href="/hr-system/assets/css/lib/themify-icons.css" rel="stylesheet">
    <link href="/hr-system/assets/css/lib/sidebar.css" rel="stylesheet">

    <script type="text/javascript" src="/hr-system/assets/js/core/libraries/jquery.min.js"></script>
    <script type="text/javascript" src="/hr-system/assets/js/core/libraries/bootstrap.min.js"></script>
    <script type="text/javascript" src="/hr-system/assets/datatables/js/jquery.dataTables.min.js"></script>

</head>

<body>
    <div class="sidebar sidebar-hide-to-small sidebar-shrink sidebar-gestures" style="background: #139ec9">
        <div class="nano">
            <div class="nano-content">
                <div class="logo" style="background: #139ec9"><span>Foxconn</span></div>
                <ul>

                    <li class="active"><a class="sidebar-sub-toggle"><i class="ti-home"></i> Menu</a>
                        <ul>
                            <li><a href="index.html">Menu 1</a></li>
                        </ul>

                    <li><a class="sidebar-sub-toggle"><i class="ti-bar-chart-alt"></i> Menu1 <span
                                class="sidebar-collapse-icon ti-angle-down"></span></a>
                        <ul>
                            <li><a href="#">Flot</a></li>
                        </ul>
                    <li><a href="#"><i class="ti-calendar"></i> Menu1 </a></li>
                    <li><a class="sidebar-sub-toggle"><i class="ti-layout"></i> Menu1 <span
                                class="sidebar-collapse-icon ti-angle-down"></span></a>
                        <ul>
                            <li><a href="#">Menu1</a></li>

                        </ul>
                    </li>
                    <li><a class="sidebar-sub-toggle"><i class="ti-panel"></i> Menu1 <span
                                class="sidebar-collapse-icon ti-angle-down"></span></a>
                        <ul>
                            <li><a href="#">Menu1</a></li>
                        </ul>
                    </li>
                </ul>
            </div>
        </div>
    </div>
    <!-- /# sidebar -->

    <div class="content-wrap">
        <div class="main">
            <div class="container-fluid">

                    <div class="header" style="background:  #139ec9">
                            <div class="container-fluid">
                                <div class="row">
                                    <div class="col-lg-12">
                                        <div class="float-left">
                                            <div class="hamburger sidebar-toggle">
                                                <span class="line"></span>
                                                <span class="line"></span>
                                                <span class="line"></span>

                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    <div class="banner" style="background: rgba(185, 216, 224, 0.418);">
                            <div class="container-fluid">
                                <div class="row">
                                    <div class="col-lg-2">
                                    </div>
                                    <div class="col-lg-8">
                                        <div class="ads"><img src="/hr-system/assets/images/custom/bannerfc.png" style="width:100%; height:100%"></div>
                                    </div>
                                </div>
                            </div>
                        </div>
                <!--Mid nav-->
                <div class="mid-nav" id="myMidnav">
                    <div class="topButton col-lg-12 col-md-12 col-sm-12 col-xs-12">
                        <label for="file" id="choose"><b style="margin-right: 2em;color: blue; cursor: pointer; "><i class="fa fa-file-excel-o"></i>Choose File</b></label>
                        <input id="file" type="file" name="photo" style="display: none;">
                        <lable for="file" class="sendall"  onclick="sendAll()"><b style="margin-right: 2em;color: blue; cursor: pointer; "><i class="fa fa-paper-plane"></i>Send all</b></a>
                    </div>

                </div>
                <!--End Mid nav-->

                <!--table-->

                <div class="table-wrapper col-lg-12 col-md-12 col-sm-12 col-xs-12" style="overflow-x: auto">
                    <table class="table table-striped table-hover table-bordered table-sm" cellspacing="0" width="100%"
                        id="wrapper">
                        <thead>
                            <tr>
                                <th width="30px">行动</th>
                                <th>#</th>
                                <th>法人</th>
                                <th>事業群</th>
                                <th>部門</th>
                                <th>費用代碼</th>
                                <th>姓名</th>
                                <th>工號</th>
                                <th>現資位</th>
                                <th>現管理職</th>
                                <th>匯率USD:VND</th>
                                <th>入職日期 </th>
                                <th>當月總日數</th>
                                <th>基本薪資USD/月</th>
                                <th>工號</th>
                                <th>當月停留天數</th>
                                <th>日津貼額HKD</th>
                                <th>應領津貼HKD</th>
                                <th>加項金額HKD</th>
                                <th>交通費HKD</th>
                                <th>稅前個人應領金額HKD</th>
                                <th>稅前個人應領金額USD</th>
                                <th>實際只應扣個人之稅額</th>
                                <th>稅後個人應領金額USD</th>
                                <th>匯入員工越南帳號USD</th>
                                <th>中港津貼補發USD</th>
                                <th>中港津貼補發USD</th>
                                <th>中港津貼補發HKD</th>
                                <th>備註</th>

                            </tr>
                        </thead>
                        <tbody id="infoEmp">

                        </tbody>
                    </table>
                </div>
                <div id="pageNavPosition" style="text-align: right"></div>

                <!--footer-->
                <div class="footer" style="width: auto;">
                    <div class="footer-copyright text-center py-1 col-lg-12 col-md-12" style="background: #139ec9">
                        &copy; 2019 Copyright</div>
                </div>
            </div>
        </div>
    </div>
</body>

<script type="text/javascript" src="/hr-system/assets/js/custom/hr.js"></script>

<script type="text/javascript" src="/hr-system/assets/js/lib/jquery.nanoscroller.min.js"></script>
<script type="text/javascript" src="/hr-system/assets/js/lib/sidebar.js"></script>

</html>