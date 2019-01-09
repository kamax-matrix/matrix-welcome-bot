# Matrix Welcome Bot - Greet your new room members with a message
# Copyright (C) 2019 Kamax Sarl
#
# https://www.kamax.io/
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.

FROM openjdk:8-jre-slim

ADD src/docker/start.sh /app/start.sh
ADD build/libs/matrix-welcome-bot.jar /app/app.jar

WORKDIR /app
CMD [ "/app/start.sh" ]
