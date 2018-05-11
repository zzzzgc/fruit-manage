webpackJsonp([38],{"ERY/":function(t,e,o){"use strict";var a=o("woOf"),r=o.n(a),d=o("g7ZD");e.a={components:{},name:"warehouseOutEntertainAddOrEdit",props:["showDialog","importParam"],mounted:function(){this.show()},data:function(){return{on_off:{isSelectProduct:!1,tempProductInfoLoading:!1,isEdit:!1},resetForm:{out_type:1},addOrEditInfo:{out_type:1},tempProduct:{},tempProductsInfo:[],tempProductStandardsInfo:[]}},methods:{show:function(){var t=this;this.reset(),null!=this.importParam.id&&this.$http.post("/manage/warehouse/out/detail/getWarehouseDetailInfo",{id:this.importParam.id}).then(function(e){t.addOrEditInfo=e.data})},reset:function(){this.$refs.form&&this.$refs.form.resetFields();try{this.addOrEditInfo=r()({},this.resetForm)}catch(t){}},successMsg:function(t){this.$message({message:t,type:"success"})},warningMsg:function(t){this.$message({message:t,type:"warning"})},onSubmit:function(){var t=this;console.log(r()({},this.addOrEditInfo,this.tempProduct,this.importParam)),this.addOrEditInfo.out_Id=this.importParam.out_id,this.$http.post("/manage/warehouse/out/detail/editWarehouseDetailInfo",r()({},this.addOrEditInfo,this.tempProduct,this.importParam)).then(function(e){t.successMsg("提交成功"),d.c.$emit(d.s.refreshListForEdit)},function(){t.warningMsg("提交失败")})},productSearch:function(t){var e=this;""!==t?(this.on_off.tempProductInfoLoading=!0,this.$http.post("/manage/order/getProductInfoByQuery",{queryString:t}).then(function(t){e.on_off.tempProductInfoLoading=!1,e.tempProductsInfo=t.data})):this.tempProductsInfo=""},selectProduct:function(t){var e=this;console.log("选中商品啦"),console.log(t);for(var o=this.tempProductsInfo,a=0;a<o.length;a++)if(o[a].product_id===t){this.tempProduct=r()({},this.tempProduct,o[a]);break}this.$http.post("/manage/order/getProductIdStandardsInfo",{productId:t}).then(function(t){console.log("获取商品规格信息"),console.log(t.data),e.tempProductStandardsInfo=t.data,e.on_off.isSelectProduct=!0})},selectProductStandard:function(t){for(var e=this.tempProductStandardsInfo,o=0;o<e.length;o++)if(e[o].product_standard_id===t){this.tempProduct=r()({},this.tempProduct,e[o]),this.addOrEditInfo.product_standard_id=this.tempProduct.product_standard_id;break}}}}},U3sY:function(t,e,o){var a=o("uaR0");"string"==typeof a&&(a=[[t.i,a,""]]),a.locals&&(t.exports=a.locals);o("rjj0")("1295d2d0",a,!0)},VtXX:function(t,e,o){"use strict";function a(t){o("U3sY")}Object.defineProperty(e,"__esModule",{value:!0});var r=o("ERY/"),d=o("ZdOm"),i=o("VU/8"),n=a,s=i(r.a,d.a,!1,n,"data-v-3affb68d",null);e.default=s.exports},ZdOm:function(t,e,o){"use strict";var a=function(){var t=this,e=t.$createElement,o=t._self._c||e;return o("el-dialog",{attrs:{visible:t.showDialog,visible:t.showDialog,"close-on-click-modal":!1},on:{"update:visible":[function(e){t.showDialog=e},function(e){return t.$emit("update:showDialog",e)}],close:t.reset,open:t.show}},[o("el-form",{attrs:{"label-width":"80px","label-position":"right"}},[o("el-row",[t.importParam.id?o("el-col",{attrs:{span:10}},[o("el-form-item",{attrs:{label:"商品名"}},[t._v("\n          "+t._s(t.addOrEditInfo.product_name)+"\n        ")])],1):t._e(),t._v(" "),t.importParam.id?o("el-col",{attrs:{span:8}},[o("el-form-item",{attrs:{label:"规格名"}},[t._v("\n          "+t._s(t.addOrEditInfo.product_standard_name)+"\n        ")])],1):t._e(),t._v(" "),t.importParam.id?t._e():o("el-col",{attrs:{span:10}},[o("el-form-item",{attrs:{label:"商品名称"}},[o("el-select",{staticStyle:{width:"100%"},attrs:{size:"mini",filterable:"",remote:"","remote-method":t.productSearch,placeholder:"请输入关键词",loading:t.on_off.tempProductInfoLoading},on:{change:t.selectProduct},model:{value:t.addOrEditInfo.product_id,callback:function(e){t.$set(t.addOrEditInfo,"product_id",e)},expression:"addOrEditInfo.product_id"}},t._l(t.tempProductsInfo,function(t){return o("el-option",{key:t.product_id,attrs:{label:t.product_name+"["+t.brand+"]",value:t.product_id}})}))],1)],1),t._v(" "),t.importParam.id?t._e():o("el-col",{attrs:{span:8}},[o("el-form-item",{attrs:{label:"规格名称"}},[o("el-select",{staticStyle:{width:"100%"},attrs:{size:"mini",placeholder:"请输入关键词"},on:{change:t.selectProductStandard},model:{value:t.addOrEditInfo.product_standard_id,callback:function(e){t.$set(t.addOrEditInfo,"product_standard_id",e)},expression:"addOrEditInfo.product_standard_id"}},t._l(t.tempProductStandardsInfo,function(t){return o("el-option",{key:t.product_standard_id,attrs:{label:t.product_standard_name+"["+t.product_standard_id+"]",value:t.product_standard_id}})}))],1)],1),t._v(" "),o("el-col",{attrs:{span:6}},[o("el-form-item",{attrs:{label:"规格编号"}},[t._v("\n          "+t._s(t.addOrEditInfo.product_standard_id)+"\n        ")])],1),t._v(" "),o("el-col",{attrs:{span:8}},[o("el-form-item",{attrs:{label:"重量(斤)"}},[o("el-input",{attrs:{size:"mini",placeholder:"请输入"},model:{value:t.addOrEditInfo.product_weight,callback:function(e){t.$set(t.addOrEditInfo,"product_weight",e)},expression:"addOrEditInfo.product_weight"}})],1)],1),t._v(" "),o("el-col",{attrs:{span:8}},[o("el-form-item",{attrs:{label:"出库数量"}},[o("el-input",{attrs:{size:"mini",placeholder:"请输入"},model:{value:t.addOrEditInfo.out_num,callback:function(e){t.$set(t.addOrEditInfo,"out_num",e)},expression:"addOrEditInfo.out_num"}})],1)],1),t._v(" "),o("el-col",{attrs:{span:24}},[o("el-form-item",{attrs:{label:"招待时间"}},[o("el-date-picker",{attrs:{"value-format":"yyyy-MM-dd HH:mm:ss",type:"datetime",size:"mini",placeholder:"请选择时间"},model:{value:t.addOrEditInfo.out_time,callback:function(e){t.$set(t.addOrEditInfo,"out_time",e)},expression:"addOrEditInfo.out_time"}})],1)],1),t._v(" "),o("el-col",{attrs:{span:8}},[o("el-form-item",{attrs:{label:"领用人"}},[o("el-input",{attrs:{size:"mini",placeholder:"请输入"},model:{value:t.addOrEditInfo.user_name,callback:function(e){t.$set(t.addOrEditInfo,"user_name",e)},expression:"addOrEditInfo.user_name"}})],1)],1),t._v(" "),o("el-col",{attrs:{span:8}},[o("el-form-item",{attrs:{label:"审批人"}},[o("el-input",{attrs:{size:"mini",placeholder:"请输入"},model:{value:t.addOrEditInfo.approver_name,callback:function(e){t.$set(t.addOrEditInfo,"approver_name",e)},expression:"addOrEditInfo.approver_name"}})],1)],1),t._v(" "),o("el-col",{attrs:{span:24}},[o("el-form-item",{attrs:{label:"招待备注"}},[o("el-input",{attrs:{size:"mini",placeholder:"请输入"},model:{value:t.addOrEditInfo.out_remark,callback:function(e){t.$set(t.addOrEditInfo,"out_remark",e)},expression:"addOrEditInfo.out_remark"}})],1)],1)],1)],1),t._v(" "),o("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[o("el-button",{attrs:{type:"primary"},on:{click:t.onSubmit}},[t._v("确 定")]),t._v(" "),o("el-button",{on:{click:function(e){t.$emit("update:showDialog",!1)}}},[t._v("取 消")])],1)],1)},r=[],d={render:a,staticRenderFns:r};e.a=d},uaR0:function(t,e,o){e=t.exports=o("FZ+f")(!1),e.push([t.i,"",""])}});