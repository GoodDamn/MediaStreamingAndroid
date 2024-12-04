precision mediump float;
uniform vec2 u_res;
uniform sampler2D u_tex;

void main() {
    vec2 n = vec2(
        gl_FragCoord.x / u_res.x,
        (u_res.y - gl_FragCoord.y) / u_res.y
    );

    vec4 pixel = texture2D(
        u_tex,
        n
    );

    gl_FragColor = vec4(
        pixel
    );
}