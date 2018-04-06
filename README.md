# PointIsInsideGeoFence


It’s important to test if a user’s device is located within a geo-fence on HeyHub.

Using the supplied JSON file, hub.json, write a simple method that checks whether a given longitude, latitude pair lie within the supplied geo-fence. Here is the method signature:

𝑓𝑢𝑛𝑐 𝑖𝑠𝐿𝑜𝑐𝑎𝑡𝑖𝑜𝑛𝑊𝑖𝑡h𝑖𝑛𝐴𝑟𝑒𝑎(𝑙𝑜𝑛𝑔𝑖𝑡𝑢𝑑𝑒: 𝐷𝑜𝑢𝑏𝑙𝑒, 𝑙𝑎𝑡𝑖𝑡𝑢𝑑𝑒: 𝐷𝑜𝑢𝑏𝑙𝑒, 𝑎𝑐𝑐𝑢𝑟𝑎𝑐𝑦: 𝐷𝑜𝑢𝑏𝑙𝑒) → 𝐵𝑜𝑜𝑙

The ‘accuracy’ parameter accounts for the inaccuracy of the lookup. The function should accept points that lie outside with geofence, but are still within the distance of the accuracy parameter. For example: If the accuracy = 60, a point that is 30 metres outside of the boundary area should still return true, but if the point lies 90 metres outside the area, the function should return false.

The function should work with ANY supplied JSON file in this format, not just the file supplied.

This is what the JSON file hub.json looks like when plotted on a map:
