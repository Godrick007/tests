precision mediump float;
varying vec2 ft_position;
uniform sampler2D sTexture;
void main(){
    gl_FragColor = texture(sTexture,ft_position);
}


