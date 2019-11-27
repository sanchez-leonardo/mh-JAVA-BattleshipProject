<script>
export default {
  funtional: true,

  name: "DynamicImage",

  props: ["image", "alt"],
  render(createElement) {
    return createElement("img", {
      attrs: {
        src: require("../../assets/" + this.image),
        alt: this.alt + " image",
        draggable: false
      }
    });
  }
};
</script>