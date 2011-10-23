package cldellow.ballero.test


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
}

// vim: set ts=2 sw=2 et:
