<template>
  <td class="text-left">
    <img v-for="(dmg, key) in damages" :key="key" :src="require('../../assets/'+ dmg + 'hit.png')" />
  </td>
</template>

<script>
export default {
  name: "FleetStatusDamageCell",

  props: { shipType: String, damage: Number },

  computed: {
    damages() {
      let imagesToRender = new Array();
      switch (this.shipType) {
        case "carrier":
          imagesToRender.length = 5;
          imagesToRender.fill("green", 0, 5);
          imagesToRender.fill("red", 0, this.damage);
          break;

        case "battleship":
          imagesToRender.length = 4;
          imagesToRender.fill("green", 0, 4);
          imagesToRender.fill("red", 0, this.damage);
          break;

        case "destroyer":
          imagesToRender.length = 3;
          imagesToRender.fill("green", 0, 3);
          imagesToRender.fill("red", 0, this.damage);
          break;

        case "submarine":
          imagesToRender.length = 3;
          imagesToRender.fill("green", 0, 3);
          imagesToRender.fill("red", 0, this.damage);
          break;

        case "patrol_boat":
          imagesToRender.length = 2;
          imagesToRender.fill("green", 0, 2);
          imagesToRender.fill("red", 0, this.damage);
          break;

        default:
          return imagesToRender;
      }
      return imagesToRender;
    }
  }
};
</script>
