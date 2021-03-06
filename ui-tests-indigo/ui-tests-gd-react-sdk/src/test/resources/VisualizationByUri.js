import React, { Component } from 'react';
import { CatalogHelper } from '@gooddata/react-components';
import catalogJson from './catalog.json';
import { Visualization } from '@gooddata/react-components';
import '@gooddata/react-components/styles/css/main.css';

var variables = require('./testing-variable.json');
const visualizationUrl = variables.visualizationUrl;
const testingProjectId = catalogJson.projectId;

class App extends Component {
    render() {
        return (
            <div style={{ height: 400, width: 600 }}>
                <Visualization
                    projectId = {testingProjectId}
                    uri = {visualizationUrl}
                />
            </div>
        );
    }
}

export default App;
