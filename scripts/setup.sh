#!/usr/bin/env bash
#
# Copyright © 2021 Kif Contributors (https://kif.firkin.io/)
# Copyright © 2021 Firkin IO (https://firkin.io/)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

jenv shell graalvm64-11.0.10
sdk use java 21.0.0.r11-grl > /dev/null
# Run these from the root 'kif' folder
alias kifkat=`pwd`/kif-cli/target/kifkat
alias kif=`pwd`/kif-cli/target/kif