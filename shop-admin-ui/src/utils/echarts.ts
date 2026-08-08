// ECharts 按需引入，只加载项目中实际使用的组件
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { TooltipComponent, GridComponent, LegendComponent, GraphicComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

echarts.use([LineChart, TooltipComponent, GridComponent, LegendComponent, GraphicComponent, CanvasRenderer])

export default echarts
