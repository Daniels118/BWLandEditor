/* Copyright (c) 2026 Daniele Lombardi / Daniels118
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
[VS]
#version 130

uniform mat4 u_projViewTrans;
uniform float u_shift;

in vec3 a_position;
in float a_len;

out float v_shift;

void main() {
	v_shift = a_len / 20.0 + u_shift;
	gl_Position = u_projViewTrans * vec4(a_position, 1.0);
}


[FS]
#version 130

in float v_shift;

out vec4 fragColor;

void main(void) {
	if (fract(v_shift) < 0.5) {
		fragColor = vec4(1.0, 1.0, 1.0, 1.0);
	} else {
		discard;
	}
}
