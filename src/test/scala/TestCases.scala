package com.cldellow.ballero.test


object Inputs {
  val GoogleResponse1 = """
{
   "results" : [
      {
         "address_components" : [
            {
               "long_name" : "285",
               "short_name" : "285",
               "types" : [ "street_number" ]
            },
            {
               "long_name" : "Bedford Ave",
               "short_name" : "Bedford Ave",
               "types" : [ "route" ]
            },
            {
               "long_name" : "Williamsburg",
               "short_name" : "Williamsburg",
               "types" : [ "neighborhood", "political" ]
            },
            {
               "long_name" : "Brooklyn",
               "short_name" : "Brooklyn",
               "types" : [ "sublocality", "political" ]
            },
            {
               "long_name" : "New York",
               "short_name" : "New York",
               "types" : [ "locality", "political" ]
            },
            {
               "long_name" : "Kings",
               "short_name" : "Kings",
               "types" : [ "administrative_area_level_2", "political" ]
            },
            {
               "long_name" : "New York",
               "short_name" : "NY",
               "types" : [ "administrative_area_level_1", "political" ]
            },
            {
               "long_name" : "United States",
               "short_name" : "US",
               "types" : [ "country", "political" ]
            },
            {
               "long_name" : "11211",
               "short_name" : "11211",
               "types" : [ "postal_code" ]
            }
         ],
         "formatted_address" : "285 Bedford Ave, Brooklyn, NY 11211, USA",
         "geometry" : {
            "location" : {
               "lat" : 40.71412890,
               "lng" : -73.96140740
            },
            "location_type" : "ROOFTOP",
            "viewport" : {
               "northeast" : {
                  "lat" : 40.71547788029149,
                  "lng" : -73.96005841970849
               },
               "southwest" : {
                  "lat" : 40.71277991970850,
                  "lng" : -73.96275638029151
               }
            }
         },
         "types" : [ "street_address" ]
      },
      {
         "address_components" : [
            {
               "long_name" : "Grand St - Bedford Av",
               "short_name" : "Grand St - Bedford Av",
               "types" : [ "bus_station", "transit_station" ]
            },
            {
               "long_name" : "Williamsburg",
               "short_name" : "Williamsburg",
               "types" : [ "neighborhood", "political" ]
            },
            {
               "long_name" : "Brooklyn",
               "short_name" : "Brooklyn",
               "types" : [ "sublocality", "political" ]
            },
            {
               "long_name" : "Kings",
               "short_name" : "Kings",
               "types" : [ "administrative_area_level_2", "political" ]
            },
            {
               "long_name" : "New York",
               "short_name" : "NY",
               "types" : [ "administrative_area_level_1", "political" ]
            },
            {
               "long_name" : "United States",
               "short_name" : "US",
               "types" : [ "country", "political" ]
            },
            {
               "long_name" : "11211",
               "short_name" : "11211",
               "types" : [ "postal_code" ]
            }
         ],
         "formatted_address" : "Grand St - Bedford Av, Brooklyn, NY 11211, USA",
         "geometry" : {
            "location" : {
               "lat" : 40.7143210,
               "lng" : -73.9611510
            },
            "location_type" : "APPROXIMATE",
            "viewport" : {
               "northeast" : {
                  "lat" : 40.71566998029149,
                  "lng" : -73.95980201970849
               },
               "southwest" : {
                  "lat" : 40.71297201970850,
                  "lng" : -73.96249998029151
               }
            }
         },
         "types" : [ "bus_station", "transit_station" ]
      },
      {
         "address_components" : [
            {
               "long_name" : "Grand St - Bedford Av",
               "short_name" : "Grand St - Bedford Av",
               "types" : [ "bus_station", "transit_station" ]
            },
            {
               "long_name" : "Williamsburg",
               "short_name" : "Williamsburg",
               "types" : [ "neighborhood", "political" ]
            },
            {
               "long_name" : "Brooklyn",
               "short_name" : "Brooklyn",
               "types" : [ "sublocality", "political" ]
            },
            {
               "long_name" : "Kings",
               "short_name" : "Kings",
               "types" : [ "administrative_area_level_2", "political" ]
            },
            {
               "long_name" : "New York",
               "short_name" : "NY",
               "types" : [ "administrative_area_level_1", "political" ]
            },
            {
               "long_name" : "United States",
               "short_name" : "US",
               "types" : [ "country", "political" ]
            },
            {
               "long_name" : "11211",
               "short_name" : "11211",
               "types" : [ "postal_code" ]
            }
         ],
         "formatted_address" : "Grand St - Bedford Av, Brooklyn, NY 11211, USA",
         "geometry" : {
            "location" : {
               "lat" : 40.7146840,
               "lng" : -73.9615630
            },
            "location_type" : "APPROXIMATE",
            "viewport" : {
               "northeast" : {
                  "lat" : 40.71603298029149,
                  "lng" : -73.96021401970850
               },
               "southwest" : {
                  "lat" : 40.71333501970850,
                  "lng" : -73.96291198029151
               }
            }
         },
         "types" : [ "bus_station", "transit_station" ]
      },
      {
         "address_components" : [
            {
               "long_name" : "Bedford Av - Grand St",
               "short_name" : "Bedford Av - Grand St",
               "types" : [ "bus_station", "transit_station" ]
            },
            {
               "long_name" : "Williamsburg",
               "short_name" : "Williamsburg",
               "types" : [ "neighborhood", "political" ]
            },
            {
               "long_name" : "Brooklyn",
               "short_name" : "Brooklyn",
               "types" : [ "sublocality", "political" ]
            },
            {
               "long_name" : "Kings",
               "short_name" : "Kings",
               "types" : [ "administrative_area_level_2", "political" ]
            },
            {
               "long_name" : "New York",
               "short_name" : "NY",
               "types" : [ "administrative_area_level_1", "political" ]
            },
            {
               "long_name" : "United States",
               "short_name" : "US",
               "types" : [ "country", "political" ]
            },
            {
               "long_name" : "11211",
               "short_name" : "11211",
               "types" : [ "postal_code" ]
            }
         ],
         "formatted_address" : "Bedford Av - Grand St, Brooklyn, NY 11211, USA",
         "geometry" : {
            "location" : {
               "lat" : 40.714710,
               "lng" : -73.9609990
            },
            "location_type" : "APPROXIMATE",
            "viewport" : {
               "northeast" : {
                  "lat" : 40.71605898029150,
                  "lng" : -73.95965001970849
               },
               "southwest" : {
                  "lat" : 40.71336101970850,
                  "lng" : -73.96234798029150
               }
            }
         },
         "types" : [ "bus_station", "transit_station" ]
      },
      {
         "address_components" : [
            {
               "long_name" : "Williamsburg",
               "short_name" : "Williamsburg",
               "types" : [ "neighborhood", "political" ]
            },
            {
               "long_name" : "Brooklyn",
               "short_name" : "Brooklyn",
               "types" : [ "sublocality", "political" ]
            },
            {
               "long_name" : "New York",
               "short_name" : "New York",
               "types" : [ "locality", "political" ]
            },
            {
               "long_name" : "Kings",
               "short_name" : "Kings",
               "types" : [ "administrative_area_level_2", "political" ]
            },
            {
               "long_name" : "New York",
               "short_name" : "NY",
               "types" : [ "administrative_area_level_1", "political" ]
            },
            {
               "long_name" : "United States",
               "short_name" : "US",
               "types" : [ "country", "political" ]
            }
         ],
         "formatted_address" : "Williamsburg, Brooklyn, NY, USA",
         "geometry" : {
            "bounds" : {
               "northeast" : {
                  "lat" : 40.7182050,
                  "lng" : -73.9202810
               },
               "southwest" : {
                  "lat" : 40.6979330,
                  "lng" : -73.96984510
               }
            },
            "location" : {
               "lat" : 40.70644610,
               "lng" : -73.95361629999999
            },
            "location_type" : "APPROXIMATE",
            "viewport" : {
               "northeast" : {
                  "lat" : 40.7182050,
                  "lng" : -73.9202810
               },
               "southwest" : {
                  "lat" : 40.6979330,
                  "lng" : -73.96984510
               }
            }
         },
         "types" : [ "neighborhood", "political" ]
      },
      {
         "address_components" : [
            {
               "long_name" : "11211",
               "short_name" : "11211",
               "types" : [ "postal_code" ]
            },
            {
               "long_name" : "Brooklyn",
               "short_name" : "Brooklyn",
               "types" : [ "sublocality", "political" ]
            },
            {
               "long_name" : "New York",
               "short_name" : "New York",
               "types" : [ "locality", "political" ]
            },
            {
               "long_name" : "New York",
               "short_name" : "NY",
               "types" : [ "administrative_area_level_1", "political" ]
            },
            {
               "long_name" : "United States",
               "short_name" : "US",
               "types" : [ "country", "political" ]
            }
         ],
         "formatted_address" : "Brooklyn, NY 11211, USA",
         "geometry" : {
            "bounds" : {
               "northeast" : {
                  "lat" : 40.7280090,
                  "lng" : -73.92072990
               },
               "southwest" : {
                  "lat" : 40.69763590,
                  "lng" : -73.97616690
               }
            },
            "location" : {
               "lat" : 40.71800360,
               "lng" : -73.96537150000002
            },
            "location_type" : "APPROXIMATE",
            "viewport" : {
               "northeast" : {
                  "lat" : 40.7280090,
                  "lng" : -73.92072990
               },
               "southwest" : {
                  "lat" : 40.69763590,
                  "lng" : -73.97616690
               }
            }
         },
         "types" : [ "postal_code" ]
      },
      {
         "address_components" : [
            {
               "long_name" : "Brooklyn",
               "short_name" : "Brooklyn",
               "types" : [ "sublocality", "political" ]
            },
            {
               "long_name" : "New York",
               "short_name" : "New York",
               "types" : [ "locality", "political" ]
            },
            {
               "long_name" : "Kings",
               "short_name" : "Kings",
               "types" : [ "administrative_area_level_2", "political" ]
            },
            {
               "long_name" : "New York",
               "short_name" : "NY",
               "types" : [ "administrative_area_level_1", "political" ]
            },
            {
               "long_name" : "United States",
               "short_name" : "US",
               "types" : [ "country", "political" ]
            }
         ],
         "formatted_address" : "Brooklyn, NY, USA",
         "geometry" : {
            "bounds" : {
               "northeast" : {
                  "lat" : 40.7394460,
                  "lng" : -73.8333650
               },
               "southwest" : {
                  "lat" : 40.55104190,
                  "lng" : -74.056630
               }
            },
            "location" : {
               "lat" : 40.650,
               "lng" : -73.950
            },
            "location_type" : "APPROXIMATE",
            "viewport" : {
               "northeast" : {
                  "lat" : 40.7394460,
                  "lng" : -73.8333650
               },
               "southwest" : {
                  "lat" : 40.55104190,
                  "lng" : -74.056630
               }
            }
         },
         "types" : [ "sublocality", "political" ]
      },
      {
         "address_components" : [
            {
               "long_name" : "Kings",
               "short_name" : "Kings",
               "types" : [ "administrative_area_level_2", "political" ]
            },
            {
               "long_name" : "New York",
               "short_name" : "NY",
               "types" : [ "administrative_area_level_1", "political" ]
            },
            {
               "long_name" : "United States",
               "short_name" : "US",
               "types" : [ "country", "political" ]
            }
         ],
         "formatted_address" : "Kings, New York, USA",
         "geometry" : {
            "bounds" : {
               "northeast" : {
                  "lat" : 40.7394460,
                  "lng" : -73.8333650
               },
               "southwest" : {
                  "lat" : 40.55104190,
                  "lng" : -74.056630
               }
            },
            "location" : {
               "lat" : 40.65287620,
               "lng" : -73.95949399999999
            },
            "location_type" : "APPROXIMATE",
            "viewport" : {
               "northeast" : {
                  "lat" : 40.7394460,
                  "lng" : -73.8333650
               },
               "southwest" : {
                  "lat" : 40.55104190,
                  "lng" : -74.056630
               }
            }
         },
         "types" : [ "administrative_area_level_2", "political" ]
      },
      {
         "address_components" : [
            {
               "long_name" : "New York",
               "short_name" : "New York",
               "types" : [ "locality", "political" ]
            },
            {
               "long_name" : "New York",
               "short_name" : "New York",
               "types" : [ "administrative_area_level_2", "political" ]
            },
            {
               "long_name" : "New York",
               "short_name" : "NY",
               "types" : [ "administrative_area_level_1", "political" ]
            },
            {
               "long_name" : "United States",
               "short_name" : "US",
               "types" : [ "country", "political" ]
            }
         ],
         "formatted_address" : "New York, NY, USA",
         "geometry" : {
            "bounds" : {
               "northeast" : {
                  "lat" : 40.9175770,
                  "lng" : -73.7002720
               },
               "southwest" : {
                  "lat" : 40.4773990,
                  "lng" : -74.259090
               }
            },
            "location" : {
               "lat" : 40.71435280,
               "lng" : -74.00597309999999
            },
            "location_type" : "APPROXIMATE",
            "viewport" : {
               "northeast" : {
                  "lat" : 40.9175770,
                  "lng" : -73.7002720
               },
               "southwest" : {
                  "lat" : 40.4773990,
                  "lng" : -74.259090
               }
            }
         },
         "types" : [ "locality", "political" ]
      }
   ],
   "status" : "OK"
}"""

  val GoogleResponse2 = """
{   "results" : [      {         "address_components" : [            {               "long_name" : "Antarctica",               "short_name" : "AQ",               "types" : [ "country", "political" ]            }         ],         "formatted_address" : "Antarctica",         "geometry" : {            "bounds" : {               "northeast" : {                  "lat" : -60.10870,                  "lng" : 180.0               },               "southwest" : {                  "lat" : -90.0,                  "lng" : -180.0               }            },            "location" : {               "lat" : -75.2509730,               "lng" : -0.07138899999999999            },            "location_type" : "APPROXIMATE",            "viewport" : {               "northeast" : {                  "lat" : -24.91887080,                  "lng" : 131.06142650               },               "southwest" : {                  "lat" : -86.99237450,                  "lng" : -131.20420450               }            }         },         "types" : [ "country", "political" ]      }   ],   "status" : "OK"}
  """

  val JennsNeedles = """
  <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html lang='en' xml:lang='en' xmlns='http://www.w3.org/1999/xhtml'>
<head>
<title>Ravelry: jkdellow's Needle and Hook Inventory</title>
<meta content='text/html; charset=utf-8' http-equiv='Content-Type' />
<meta content='UuHzBCO1xRLgxzmoAjzsFGjgqkHCMxuDdObdOhaACV8=' id='authenticity-token' name='authenticity-token' />
<meta content='noodp' name='robots' />
<link href="http://style3.ravelrycache.com/stylesheets/ravelry_global_1109301816.css" media="screen" rel="Stylesheet" type="text/css" />
<link href="http://style0.ravelrycache.com/stylesheets/ravelry_components_1110191517.css" media="screen" rel="Stylesheet" type="text/css" />

<link href="http://style0.ravelrycache.com/stylesheets/print_801080522.css" media="print" rel="Stylesheet" type="text/css" />

<script src="http://style2.ravelrycache.com/javascripts/base11_1110072004.js" type="text/javascript"></script>
<script src="http://style1.ravelrycache.com/javascripts/ravelry_1110191520.js" type="text/javascript"></script>



<link rel="search" type="application/opensearchdescription+xml" title="Ravelry" href="http://www.ravelry.com/search.xml">
<!--[if lt IE 7]> 
<link href="http://style2.ravelrycache.com/stylesheets/ie6.r12.css" media="screen" rel="stylesheet" type="text/css" />
<![endif]-->
<!--[if lt IE 8]> 
<link href="http://style0.ravelrycache.com/stylesheets/ie.r3.css" media="screen" rel="stylesheet" type="text/css" />
<![endif]-->
</head>
<body class='notebook normal'>
<div id='page'>
<div id='banner'>
<a href="/"><img height="73" src="http://style1.ravelrycache.com/images/ravelry-logo.gif" title="Ravelry!" width="213" /></a>
</div>
<div id='status'>
<div id='messages'>
<div id='login'>
<div id='logged_in'>
hiya, 
<strong>
<a href="http://www.ravelry.com/people/cldellow" class="login">cldellow</a>!
</strong>
<a href="/account/logout" onclick="R.accounts.logout(); return false;">logout</a>
</div>
<div id='inbox'>
<span id='unread_messages_count'>

</span>
</div>

</div>
</div>
</div>
<div id='navigation'>
<ul>
<li id='notebook_tab'>
<a href="http://www.ravelry.com/projects/cldellow">my notebook</a>
<ul id='notebook_menu' style='display: none;'>
<li class='projects_option'><a href="/projects/cldellow">projects</a></li>
<li class='stash_option'><a href="/people/cldellow/stash">stash</a></li>
<li class='queue_option'><a href="/people/cldellow/queue">queue</a></li>
<li class='favorites_option'><a href="/people/cldellow/favorites">favorites</a></li>
<li class='friends_option'><a href="/people/cldellow/friends">friends</a></li>
<li class='user_groups_option'><a href="http://www.ravelry.com/people/cldellow/groups">groups/events</a></li>
<li class='needles_option'><a href="/people/cldellow/needles">needles/hooks</a></li>
<li class='books_option'><a href="http://www.ravelry.com/people/cldellow/library">library</a></li>
<li class='messages_option'><a href="/people/cldellow/messages">messages</a></li>
<li class='posts_option'><a href="/people/cldellow/posts">blog posts</a></li>
<li class='contributions_option'><a href="/people/cldellow/contributions">contributions</a></li>
</ul>
</li>
<li id='patterns_tab'><a href="/patterns">patterns</a></li>
<li><a href="/yarns">yarns</a></li>
<li class='active'><a href="/people">people</a></li>
<li><a href="http://www.ravelry.com/discuss">forums</a></li>
<li><a href="http://www.ravelry.com/groups">groups</a></li>
<li><a href="http://www.ravelry.com/shop">shop</a></li>
<li id='help_tab'>
<a href="http://www.ravelry.com/help" style="font-size: 1.2em;" title="Ravelry Help"><img height="18" src="http://style0.ravelrycache.com/images/question2-2.png" width="17" /></a>
</li>
<li id='search_tab' onclick='R.searchlight.tabClicked();'>
<a href="/search" onclick="return false;" title="Search ravelry"><img class="inline" height="16" src="http://style3.ravelrycache.com/images/fugue-magnifier_left.png" width="16" /></a>
</li>
<li id='carts_tab' style='display: none;'></li>
</ul>
</div>
<div id='searchlight' style='display: none;'>
<div class='searchlight_top'>
<a href="#" onclick="R.searchlight.close(); return false;">&nbsp;&nbsp;&nbsp;&nbsp;</a>
</div>
<div class='searchlight_content'>
<div class='section'>
<img class="inline" src="http://style2.ravelrycache.com/images/fugue-lightning.png" />
quick search
</div>
<form onsubmit='R.searchlight.search(); return false;'>
<input id='searchlight_term' type='text' />
<div id='searchlight_progress'></div>
</form>
<div id='searchlight_results'></div>
<div class='section'>
<img class="inline" src="http://style0.ravelrycache.com/images/advanced-search-tiny.png" />
advanced search
</div>
<div class='advanced_searchlight searchlight_component' id='advanced_search_links'>
<ul>
<li><a href="/patterns/search#query=" id="searchlight_patterns_advanced">patterns</a></li>
<li><a href="/projects/search#query=">projects</a></li>
<li><a href="/designers/search#query=">designers</a></li>
<li><a href="/sources/search#query=">sources</a></li>
</ul>
<ul>
<li><a href="/yarns/search#query=">yarns</a></li>
<li><a href="/stash/search#query=">stashes</a></li>
<li><a href="/yarns/brands/search#query=">yarn brands</a></li>
</ul>
<ul>
<li><a href="/people/search#query=">people</a></li>
<li><a href="/groups/search#query=">groups</a></li>
<li><a href="/discuss/search#query=&amp;view=posts">forum posts</a></li>
<li><a href="/discuss/search#query=&amp;view=topics">topics</a></li>
<li><a href="/events/search#query=">events</a></li>
</ul>
<div style="clear:both;" class="c_d"></div>

</div>
<div class='section'>
<img class="inline" height="16" src="http://style2.ravelrycache.com/images/silk-disk.png" width="16" />
my saved searches
</div>
<div class='saved_searchlight searchlight_component' id='searchlight_saved_searches'></div>
</div>
<div class='searchlight_bottom'></div>
</div>

<div id='content'>
<div class='heading'>
<h2>
<a href="http://www.ravelry.com/people/jkdellow" class="breadcrumb_avatar avatar_bubble" id="ab_jkdellow" title="View jkdellow's profile"><img height="25" src="http://avatars.ravelrycache.com/jkdellow/65255908/profile-pic_tiny.jpg" width="25" /></a>
<a href="http://www.ravelry.com/people/jkdellow" class="notebook_login">jkdellow</a>
<small><a href="http://www.ravelry.com/people/jkdellow">about me</a></small>
</h2>
</div>
<div class='notebook_page'>
<div class='sidebar'>
<div class='chiclet' id='project_menu'>
<div class='head'></div>
<ul>
<li class="first"><span class="static_tab" id="projects_tab"><a href="/projects/jkdellow">projects</a></span></li>
<li><span class="static_tab" id="stash_tab"><a href="/people/jkdellow/stash">stash</a></span></li>
<li><span class="static_tab" id="queue_tab"><a href="/people/jkdellow/queue">queue</a></span></li>
<li><span class="static_tab" id="favorites_tab"><a href="/people/jkdellow/favorites">favorites</a></span></li>
<li><span class="static_tab" id="friends_tab"><a href="/people/jkdellow/friends">friends</a></span></li>
<li><span class="static_tab" id="user_groups_tab"><a href="http://www.ravelry.com/people/jkdellow/groups">groups &amp; events</a></span></li>
<li id="current"><span class="static_tab" id="needles_tab"><a href="/people/jkdellow/needles">needles &amp; hooks</a></span></li>
<li><span class="static_tab" id="library_tab"><a href="http://www.ravelry.com/people/jkdellow/library">library</a></span></li>
<li><span class="static_tab" id="posts_tab"><a href="/people/jkdellow/posts">blog posts</a></span></li>
<li><span class="static_tab" id="contributions_tab"><a href="http://www.ravelry.com/people/jkdellow/contributions">contributions</a></span></li>
</ul>
<div class='foot'></div>
</div>
<div id="notebook_vertical"></div>
</div>
<div class='needles_details_panel panel' id='needles_panel' style='height: auto;'>
<div class='header'>
<img class="inline" height="16" src="http://style3.ravelrycache.com/images/silk-page_white_acrobat.png" width="16" />
print:
<a href="http://www.ravelry.com/people/jkdellow/needles/print" style="text-decoration: underline;">pocket card</a>
<div class='tabs' id='tabset' style='width: 780px; margin-bottom: 5px;'>
<ul>
<li><span class="static_tab" id="chart_tab"><a href="http://www.ravelry.com/people/jkdellow/needles/chart">chart - standard</a></span></li>
<li><span class="static_tab" id="tiny_tab"><a href="http://www.ravelry.com/people/jkdellow/needles/tiny">chart - tiny needles</a></span></li>
<li><span class="static_tab" id="hooks_tab"><a href="http://www.ravelry.com/people/jkdellow/needles/hooks">chart - hooks</a></span></li>

<li id="current"><span class="static_tab" id="details_tab"><a href="http://www.ravelry.com/people/jkdellow/needles/details">detailed inventory</a></span></li>
</ul>
</div>
</div>

<p>
<img class="inline" height="16" src="http://style2.ravelrycache.com/images/silk-add.png" width="16" />
You can add new needles and hooks by using the
<a href="http://www.ravelry.com/people/jkdellow/needles/chart" style="text-decoration: underline;">chart view</a>
</p>
<table class='grid bordered lined' id='needle_details' style='width: 780px; margin-bottom: 5px;'>
<thread>
<tr>
<th style='width: 6em;'>Type</th>
<th style='width: 3em;'>US</th>
<th style='width: 4em;'>Metric</th>
<th style='width: 6em;'>Length</th>
<th>Comment</th>
</tr>
</thread>
<tbody>
<tr id='needle_row_3961378'>
<td>
circular
</td>
<td class='numeric'>
4 
</td>
<td class='numeric'>
3.5
</td>
<td>
24"
</td>
<td class='needle_comment'>
<div id='needle_comment_3961378'>
<a href="#" onclick="R.needles.editComment(3961378); return false;">bamboo</a>
</div>
</td>
</tr>
<tr id='needle_row_3961382'>
<td>
circular
</td>
<td class='numeric'>
5 
</td>
<td class='numeric'>
3.75
</td>
<td>
32"
</td>
<td class='needle_comment'>
<div id='needle_comment_3961382'>
<a href="#" onclick="R.needles.editComment(3961382); return false;">steel</a>
</div>
</td>
</tr>
<tr id='needle_row_3961389'>
<td>
circular
</td>
<td class='numeric'>
7 
</td>
<td class='numeric'>
4.5
</td>
<td>
32"
</td>
<td class='needle_comment'>
<div id='needle_comment_3961389'>
<a href="#" onclick="R.needles.editComment(3961389); return false;">plastic</a>
</div>
</td>
</tr>
<tr id='needle_row_3961390'>
<td>
circular
</td>
<td class='numeric'>
8 
</td>
<td class='numeric'>
5
</td>
<td>
16"
</td>
<td class='needle_comment'>
<div id='needle_comment_3961390'>
<a href="#" onclick="R.needles.editComment(3961390); return false;">bamboo</a>
</div>
</td>
</tr>
<tr id='needle_row_3961393'>
<td>
circular
</td>
<td class='numeric'>
9 
</td>
<td class='numeric'>
5.5
</td>
<td>
24"
</td>
<td class='needle_comment'>
<div id='needle_comment_3961393'>
<a href="#" onclick="R.needles.editComment(3961393); return false;">bamboo</a>
</div>
</td>
</tr>
<tr id='needle_row_3961397'>
<td>
circular
</td>
<td class='numeric'>
10 
</td>
<td class='numeric'>
6
</td>
<td>
32"
</td>
<td class='needle_comment'>
<div id='needle_comment_3961397'>
<a href="#" onclick="R.needles.editComment(3961397); return false;">plastic</a>
</div>
</td>
</tr>
<tr id='needle_row_3961396'>
<td>
circular
</td>
<td class='numeric'>
10 
</td>
<td class='numeric'>
6
</td>
<td>
16"
</td>
<td class='needle_comment'>
<div id='needle_comment_3961396'>
<a href="#" onclick="R.needles.editComment(3961396); return false;">bamboo</a>
</div>
</td>
</tr>
<tr id='needle_row_3961399'>
<td>
circular
</td>
<td class='numeric'>

</td>
<td class='numeric'>
7
</td>
<td>
24"
</td>
<td class='needle_comment'>
<div id='needle_comment_3961399'>
<a href="#" onclick="R.needles.editComment(3961399); return false;">plastic</a>
</div>
</td>
</tr>
<tr id='needle_row_3961401'>
<td>
circular
</td>
<td class='numeric'>
13 
</td>
<td class='numeric'>
9
</td>
<td>
29"
</td>
<td class='needle_comment'>
<div id='needle_comment_3961401'>
<a href="#" onclick="R.needles.editComment(3961401); return false;">bamboo</a>
</div>
</td>
</tr>
<tr id='needle_row_3961403'>
<td>
circular
</td>
<td class='numeric'>
15 
</td>
<td class='numeric'>
10
</td>
<td>
20"
</td>
<td class='needle_comment'>
<div id='needle_comment_3961403'>
<a href="#" onclick="R.needles.editComment(3961403); return false;">steel</a>
</div>
</td>
</tr>
<tr id='needle_row_3961405'>
<td>
circular
</td>
<td class='numeric'>
17 
</td>
<td class='numeric'>
12.75
</td>
<td>
32"
</td>
<td class='needle_comment'>
<div id='needle_comment_3961405'>
<a href="#" onclick="R.needles.editComment(3961405); return false;">plastic</a>
</div>
</td>
</tr>
<tr id='needle_row_3961373'>
<td>
dp
</td>
<td class='numeric'>
2 
</td>
<td class='numeric'>
2.75
</td>
<td>
std
</td>
<td class='needle_comment'>
<div id='needle_comment_3961373'>
<a href="#" onclick="R.needles.editComment(3961373); return false;">steel</a>
</div>
</td>
</tr>
<tr id='needle_row_3961375'>
<td>
dp
</td>
<td class='numeric'>
2½
</td>
<td class='numeric'>
3
</td>
<td>
std
</td>
<td class='needle_comment'>
<div id='needle_comment_3961375'>
<a href="#" onclick="R.needles.editComment(3961375); return false;">steel</a>
</div>
</td>
</tr>
<tr id='needle_row_3961374'>
<td>
dp
</td>
<td class='numeric'>
2½
</td>
<td class='numeric'>
3
</td>
<td>
std
</td>
<td class='needle_comment'>
<div id='needle_comment_3961374'>
<a href="#" onclick="R.needles.editComment(3961374); return false;"></a>
</div>
</td>
</tr>
<tr id='needle_row_3961376'>
<td>
dp
</td>
<td class='numeric'>
4 
</td>
<td class='numeric'>
3.5
</td>
<td>
std
</td>
<td class='needle_comment'>
<div id='needle_comment_3961376'>
<a href="#" onclick="R.needles.editComment(3961376); return false;">bamboo</a>
</div>
</td>
</tr>
<tr id='needle_row_3961387'>
<td>
dp
</td>
<td class='numeric'>
6 
</td>
<td class='numeric'>
4
</td>
<td>
std
</td>
<td class='needle_comment'>
<div id='needle_comment_3961387'>
<a href="#" onclick="R.needles.editComment(3961387); return false;">bamboo</a>
</div>
</td>
</tr>
<tr id='needle_row_3961388'>
<td>
dp
</td>
<td class='numeric'>
7 
</td>
<td class='numeric'>
4.5
</td>
<td>
std
</td>
<td class='needle_comment'>
<div id='needle_comment_3961388'>
<a href="#" onclick="R.needles.editComment(3961388); return false;">bamboo</a>
</div>
</td>
</tr>
<tr id='needle_row_3961391'>
<td>
dp
</td>
<td class='numeric'>
8 
</td>
<td class='numeric'>
5
</td>
<td>
std
</td>
<td class='needle_comment'>
<div id='needle_comment_3961391'>
<a href="#" onclick="R.needles.editComment(3961391); return false;">steel</a>
</div>
</td>
</tr>
<tr id='needle_row_3961394'>
<td>
dp
</td>
<td class='numeric'>
10 
</td>
<td class='numeric'>
6
</td>
<td>
std
</td>
<td class='needle_comment'>
<div id='needle_comment_3961394'>
<a href="#" onclick="R.needles.editComment(3961394); return false;">bamboo</a>
</div>
</td>
</tr>
<tr id='needle_row_3961379'>
<td>
straight
</td>
<td class='numeric'>
4 
</td>
<td class='numeric'>
3.5
</td>
<td>
10"
</td>
<td class='needle_comment'>
<div id='needle_comment_3961379'>
<a href="#" onclick="R.needles.editComment(3961379); return false;">bamboo</a>
</div>
</td>
</tr>
<tr id='needle_row_3973475'>
<td>
straight
</td>
<td class='numeric'>
5 
</td>
<td class='numeric'>
3.75
</td>
<td>
10"
</td>
<td class='needle_comment'>
<div id='needle_comment_3973475'>
<a href="#" onclick="R.needles.editComment(3973475); return false;">metal</a>
</div>
</td>
</tr>
<tr id='needle_row_3961385'>
<td>
straight
</td>
<td class='numeric'>
6 
</td>
<td class='numeric'>
4
</td>
<td>
14"
</td>
<td class='needle_comment'>
<div id='needle_comment_3961385'>
<a href="#" onclick="R.needles.editComment(3961385); return false;">plastic</a>
</div>
</td>
</tr>
<tr id='needle_row_3961392'>
<td>
straight
</td>
<td class='numeric'>
8 
</td>
<td class='numeric'>
5
</td>
<td>
14"
</td>
<td class='needle_comment'>
<div id='needle_comment_3961392'>
<a href="#" onclick="R.needles.editComment(3961392); return false;">steel</a>
</div>
</td>
</tr>
<tr id='needle_row_3961398'>
<td>
straight
</td>
<td class='numeric'>
10 
</td>
<td class='numeric'>
6
</td>
<td>
14"
</td>
<td class='needle_comment'>
<div id='needle_comment_3961398'>
<a href="#" onclick="R.needles.editComment(3961398); return false;">plastic</a>
</div>
</td>
</tr>
</tbody>
</table>

</div>
</div>

</div>
<div style="clear:both;" class="c_d"></div>
<div id='preload_default_images'>
<span>
<span></span>
</span>
</div>
<div id='prefooter'></div>
</div>
<div id='footer'>
<div class='linkbar'>
<a href="/">Home</a>
|
<a href="/about">About Us</a>
|
<a href="/advertisers">Advertising</a>
|
<a href="/contact">Contact Us</a>
|
<img height="16" src="http://style2.ravelrycache.com/images/splash/twitter.png" style="vertical-align: top;" width="16" />
<a href="http://twitter.com/ravelry" target="_blank">@ravelry</a>
|
<a href="/donate">Donating</a>
|
<a href="/about/goodies">Goodies</a>
|
<a href="/about/guidelines">Community Guidelines</a>, 
<a href="/about/terms">Terms of Use</a>
&amp;
<a href="/about/privacy">Privacy</a>
</div>
</div>
<script type="text/javascript">
//<![CDATA[
(function() {var stamp = new Date().getTime() + ';' + Math.random();var ajs = document.createElement('script'); ajs.type = 'text/javascript'; ajs.async = true;ajs.src = 'http://www.ravelry.com/enablers/s/notebook_vertical/?s=" + stamp + "&tag=';var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ajs, s);})();
//]]>
</script>




<script type="text/javascript">
var _qoptions = { qacct: "p-5fbyv6s1V0A-c" };
(function() {
var quant = document.createElement('script'); quant.type = 'text/javascript'; quant.async = true;
quant.src = 'http://edge.quantserve.com/quant.js';
var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(quant, s);
})();
</script>
<script type="text/javascript">
var _gaq = _gaq || [];
_gaq.push(['_setAccount', 'UA-2348901-1']);
_gaq.push(['_trackPageview']);
(function() {
var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
})();
</script>
</body>
</html>"""
}

// vim: set ts=2 sw=2 et:
