/*
Copyright 2011 Ben Biddington

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.coriander.oauth.core

import collection.mutable.ListBuffer
import org.coriander.{NameValuePair, Query}

class Normalizer {
    def normalize(query : Query) : String = {
        var pairs : ListBuffer[String] = new ListBuffer[String]() 

        query.sort.foreach(nameValuePair => pairs += toString(nameValuePair))

        pairs.toList.mkString("&")
    }

	private def toString(nameValuePair : NameValuePair) : String = 
		nameValuePair.name + "=" + (if (nameValuePair.value != null) nameValuePair.value else "")
}