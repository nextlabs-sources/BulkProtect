

<script>
import { Pie } from 'vue-chartjs'

export default {
  extends: Pie,
  props: ['extMap', 'showLegend'],
  mounted () {
    var colors = [...Array(Math.min(Object.keys(this.extMap).length,9)).keys()].map(()=>'hsl(' + (Math.random()*255).toString() + ',80%,85%)').reverse();
    this.renderChart({
      labels: this.sortedExtMap.map(e => e[0]),
      datasets: [
        {
          label: 'File Types',
          backgroundColor: colors,
          hoverBackgroundColor: colors,
          data: this.sortedExtMap.map(e => e[1]),
          
        }
      ],
    },
    {
      legend: {
        position: 'bottom',
        display: false
      },
      rotation: 0
    })
  },
  computed: {
    sortedExtMap () {
      var res = Object.entries(this.extMap).sort((a, b) => b[1] - a[1])
      var idx = res.findIndex(e => e[0] === "")
      if (idx != -1) res[idx] = ["No Extension",res[idx][1]]
      var others = ["Others", res.slice(8).reduce((a,e) => a+e[1], 0)]
      res = res.slice(0,8)
      res.push(others)
      return res
    }
  }
}
</script>
