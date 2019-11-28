<template>
  <div class="available-ships">
    <div class="holder">
      <img
        :src="require('../../assets/ships/carrierhor.png')"
        class="image shadow"
        alt="carrier shadow"
        draggable="false"
      />
      <img
        :src="require('../../assets/ships/carrierhor.png')"
        id="carrier"
        class="ship image"
        alt="carrier image"
        draggable="true"
      />
    </div>
    <div class="holder">
      <img
        :src="require('../../assets/ships/battleshiphor.png')"
        class="image shadow"
        alt="battleship shadow"
        draggable="false"
      />
      <img
        :src="require('../../assets/ships/battleshiphor.png')"
        id="battleship"
        class="ship image"
        alt="battleship image"
        draggable="true"
      />
    </div>
    <div class="holder">
      <img
        :src="require('../../assets/ships/destroyerhor.png')"
        class="image shadow"
        alt="destroyer shadow"
        draggable="false"
      />
      <img
        :src="require('../../assets/ships/destroyerhor.png')"
        id="destroyer"
        class="ship image"
        alt="destroyer image"
        draggable="true"
      />
    </div>
    <div class="holder">
      <img
        :src="require('../../assets/ships/submarinehor.png')"
        class="image shadow"
        alt="submarine shadow"
        draggable="false"
      />
      <img
        :src="require('../../assets/ships/submarinehor.png')"
        id="submarine"
        class="ship image"
        alt="submarine image"
        draggable="true"
      />
    </div>
    <div class="holder">
      <img
        :src="require('../../assets/ships/patrol_boathor.png')"
        class="image shadow"
        alt="patrolboat shadow"
        draggable="false"
      />
      <img
        :src="require('../../assets/ships/patrol_boathor.png')"
        id="patrol_boat"
        class="ship image"
        alt="patrolboat image"
        draggable="true"
      />
    </div>
  </div>
</template>

<script>
export default {
  name: "ShipsContainer"
};
</script>