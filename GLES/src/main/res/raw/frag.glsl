precision mediump float;
uniform vec2 u_res;
uniform sampler2D u_tex;
uniform int u_rotationDeg;

void main() {

    vec2 n = vec2(
        (u_res.y - gl_FragCoord.y) / u_res.y,
        (u_res.x - gl_FragCoord.x) / u_res.x
    );

    vec4 pixel = texture2D(
        u_tex,
        n
    );

    gl_FragColor = pixel;
}