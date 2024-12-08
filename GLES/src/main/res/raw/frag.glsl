precision mediump float;
uniform vec2 u_res;
uniform sampler2D u_tex;
uniform int u_rotationDeg;

void main() {

    // rotate by 90 degrees clockwise
    vec2 n;
    if (u_rotationDeg > 89.0) {
        n = vec2(
            (u_res.y - gl_FragCoord.y) / u_res.y,
            (u_res.x - gl_FragCoord.x) / u_res.x
        );
    } else {
        n = vec2(
            gl_FragCoord.y / u_res.y,
            (u_res.x - gl_FragCoord.x) / u_res.x
        );
    }

    vec4 pixel = texture2D(
        u_tex,
        n
    );

    gl_FragColor = pixel;
}