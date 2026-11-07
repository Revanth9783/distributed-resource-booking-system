import json
from pathlib import Path
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parent
fixtures = json.loads((root/'fixtures.json').read_text(encoding='utf-8-sig'))
plans = root/'plans'
plans.mkdir(exist_ok=True)
names = ['baseline-10-users.jmx','load-test-50-users.jmx','load-test-sustained-100-users.jmx','load-test-500-users.jmx','Concurrent Booking Request.jmx']
for name in names:
    tree = ET.parse(root.parent/'docs/results/jmeter-results'/name)
    for collector in tree.iter('ResultCollector'):
        collector.set('enabled','false')
    for prop in tree.iter('stringProp'):
        key = prop.get('name')
        if key == 'HTTPSampler.path' and (prop.text or '').startswith('/api/slots/resource/'):
            prop.text = f"/api/slots/resource/{fixtures['resourceId']}"
        if key == 'Argument.value' and '"userId"' in (prop.text or ''):
            prop.text = json.dumps({'userId':fixtures['userId'],'slotId':fixtures['loadSlotId'],'quantity':1})
    if name == 'Concurrent Booking Request.jmx':
        sampler = next(tree.iter('HTTPSamplerProxy'))
        parent = next(element for element in tree.iter() if sampler in list(element))
        children = list(parent)
        sampler_tree = children[children.index(sampler)+1]
        processor = ET.SubElement(sampler_tree,'JSR223PostProcessor',{
            'guiclass':'TestBeanGUI','testclass':'JSR223PostProcessor',
            'testname':'Retain failed-response body in sample message','enabled':'true'
        })
        ET.SubElement(processor,'stringProp',{'name':'scriptLanguage'}).text = 'groovy'
        ET.SubElement(processor,'stringProp',{'name':'cacheKey'}).text = 'true'
        ET.SubElement(processor,'stringProp',{'name':'script'}).text = 'if (!prev.isSuccessful()) { prev.setResponseMessage(prev.getResponseMessage() + " | " + prev.getResponseDataAsString()) }'
        ET.SubElement(sampler_tree,'hashTree')
    tree.write(plans/name,encoding='utf-8',xml_declaration=True)
print('Prepared five local JMeter plans with current fixture IDs and GUI listeners disabled.')
