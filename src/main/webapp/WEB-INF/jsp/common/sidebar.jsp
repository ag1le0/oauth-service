            <!-- Main sidebar -->
			<div class="sidebar sidebar-main fii-sidebar-menu">
				<div class="sidebar-content">

                <!-- Main navigation -->
                    <div class="sidebar-category sidebar-category-visible">
                        <div class="category-content no-padding">
                            <ul class="navigation navigation-main navigation-accordion">

                                <!-- Main -->
                                 <li class="imenu" path="home"><a href="/home"><i class="icon-home4"></i> <span>Home</span></a></li>
                                 <li>
                                     <a href="#"><i class="icon-stack2"></i> <span>B04</span></a>
                                     <ul>
                                         <li class="imenu" path="b04-overview"><a data-href="/overview?factory=B04" onclick="openWindow(this.dataset.href);">Overview</a></li>
                                         <li class="imenu" path="b04-station-status"><a data-href="/aoi-analytics/aoi-status?factory=B04" onclick="openWindow(this.dataset.href);">SMT & PTH Management</a></li>
                                         <li>
                                             <a href="#">Tester Management</a>
                                             <ul>
                                                 <li class="imenu" path="b04-station-status"><a data-href="/station-status?factory=B04" onclick="openWindow(this.dataset.href);">Tester Status</a></li>
                                                 <li class="imenu" path="b04-station-detail"><a data-href="/station-detail?factory=B04" onclick="openWindow(this.dataset.href);">Tester Reason Analysis</a></li>
                                                 <li class="imenu" path="b04-station-cpk"><a data-href="station-cpk?factory=B04" onclick="openWindow(this.dataset.href);">Tester CPK Analysis</a></li>
                                                 <li class="imenu" path="b04-maintain"><a data-href="/maintain?factory=B04" onclick="openWindow(this.dataset.href);">Tester Maintain Control</a></li>
                                                 <li class="imenu" path="b04-resource"><a data-href="/resource?factory=B04" onclick="openWindow(this.dataset.href);">Engineer Information</a></li>
                                             </ul>
                                         </li>
                                         <li class="imenu" path="re-home"><a data-href="/re" onclick="openWindow(this.dataset.href);">RE Repair Management</a></li>
                                     </ul>
                                 </li>
                                 <li>
                                     <a href="#"> <span>B05</span> <i class="icon-stack2"></i></a>
                                     <ul>
                                         <li class="imenu" path="b05-station-status"><a data-href="#" onclick="openWindow(this.dataset.href);">SMT & PTH Management</a></li>
                                         <li>
                                             <a href="#">Tester Management</a>
                                             <ul>
                                                 <li class="imenu" path="b05-station-status"><a data-href="/station-status?factory=B05" onclick="openWindow(this.dataset.href);">Tester Status</a></li>
                                                 <li class="imenu" path="b05-station-detail"><a data-href="/station-detail?factory=B05" onclick="openWindow(this.dataset.href);">Tester Reason Analysis</a></li>
                                                 <li class="imenu" path="b05-resource"><a data-href="/resource?factory=B05" onclick="openWindow(this.dataset.href);">Engineer Information</a></li>
                                             </ul>
                                         </li>
                                         <li class="imenu" path="re-home"><a data-href="/re" onclick="openWindow(this.dataset.href);">RE Repair Management</a></li>
                                         <li class="imenu" path="automation-home"><a data-href="//10.224.81.70:1712" onclick="openWindow(this.dataset.href);">Automation Management</a></li>
                                     </ul>
                                 </li>
                                 <li>
                                     <a href="#"> <span>B06</span> <i class="icon-stack2"></i></a>
                                     <ul>
                                         <li class="imenu" path="b06-station-status"><a data-href="#" onclick="openWindow(this.dataset.href);">SMT & PTH Management</a></li>
                                         <li>
                                             <a href="#">Tester Management</a>
                                             <ul>
                                                 <li class="imenu" path="b06-station-status"><a data-href="/station-status?factory=B06" onclick="openWindow(this.dataset.href);">Tester Status</a></li>
                                                 <li class="imenu" path="b06-station-detail"><a data-href="/station-detail?factory=B06" onclick="openWindow(this.dataset.href);">Tester Reason Analysis</a></li>
                                                 <li class="imenu" path="b06-station-cpk"><a data-href="station-cpk?factory=B06" onclick="openWindow(this.dataset.href);">Tester CPK Analysis</a></li>
                                                 <li class="imenu" path="b06-maintain"><a data-href="/maintain?factory=B06" onclick="openWindow(this.dataset.href);">Tester Maintain Control</a></li>
                                                 <li class="imenu" path="b06-resource"><a data-href="/resource?factory=B06" onclick="openWindow(this.dataset.href);">Engineer Information</a></li>
                                             </ul>
                                         </li>
                                         <li class="imenu" path="re-home"><a data-href="/re" onclick="openWindow(this.dataset.href);">RE Repair Management</a></li>
                                     </ul>
                                 </li>
                                 <!-- /main -->

                            </ul>
                        </div>
                    </div>
                    <!-- /main navigation -->

				</div>
			</div>
			<!-- /main sidebar -->
<script src="/assets/js/custom/common.js"></script>
<script>
    activeMenu('${path}');
</script>