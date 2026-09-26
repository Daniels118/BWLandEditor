
[VS]
#version 130

#ifdef GL_ES
precision mediump float;
#endif

in vec2 a_pos;

out vec2 v_clip;

void main() {
    // Coordinate clip-space [-1,1]
    v_clip = a_pos;
    gl_Position = vec4(a_pos, 0.0, 1.0);
}



[FS]
#version 130

#ifdef GL_ES
precision mediump float;
#endif

uniform mat4 u_invProjView;
uniform vec3 u_camPos;

uniform vec3 u_zenithColor;
uniform vec3 u_horizonColor;
uniform vec3 u_belowColor;

uniform float u_gradientPower;
uniform float u_belowPower;

in vec2 v_clip;

out vec4 fragColor;

vec3 getWorldRay(vec2 clipXY) {
    vec4 farPoint = u_invProjView * vec4(clipXY, 1.0, 1.0);
    farPoint.xyz /= farPoint.w;
    return normalize(farPoint.xyz - u_camPos);
}

void main() {
    vec3 rayDir = getWorldRay(v_clip);

    float y = rayDir.y;

    vec3 color;

    if (y >= 0.0) {
        // above horizon
        float t = clamp(y, 0.0, 1.0);
        t = pow(t, u_gradientPower);

        color = mix(u_horizonColor, u_zenithColor, t);
    } else {
        // below horizon
        float t = clamp(-y, 0.0, 1.0);
        t = pow(t, u_belowPower);

        color = mix(u_horizonColor, u_belowColor, t);
    }

    fragColor = vec4(color, 1.0);
}
