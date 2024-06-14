import * as React from 'react';
import {useState} from 'react';
import {styled} from '@mui/material/styles';
import Button from '@mui/material/Button';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';
import {Container, Paper} from "@mui/material";
import api from "../api/ApiConfig";
import DataTable from "./DataTable";

const VisuallyHiddenInput = styled('input')({
    clip: 'rect(0 0 0 0)',
    clipPath: 'inset(50%)',
    height: 1,
    overflow: 'hidden',
    position: 'absolute',
    bottom: 0,
    left: 0,
    whiteSpace: 'nowrap',
    width: 1,
});

export default function InputFileUpload() {
    const paperStyle = {padding: '15px 10px', width: '100%', margin: '50px auto'};

    const [files, setFiles] = useState([]);

    const handleFileUpload = (e) => {
        if (!e.target.files) {
            return;
        }
        const file = e.target.files[0];
        console.log(file)
        if (file !== undefined) {
            if (file.type.endsWith("csv")) {
                getFiles(file).then(() => console.log("loaded success"));
            }else {
                alert("You should upload a csv file. Please try again.")
            }
        }
    };

    const getFiles = async (file) => {
        try {
            console.log(file)

            const formData = new FormData();
            formData.append('file', file)
            const config = {
                headers: {
                    'content-type': 'multipart/form-data'
                }
            }

            const response = await api.post("/api/v1/file", formData, config);

            setFiles(response.data);
            console.log(response.data);
        } catch (err) {
            console.log(err);
        }
    }


    return (
        <Container>
            <Paper elevation={3} style={paperStyle}>
                <Button
                    component="label"
                    role={undefined}
                    variant="contained"
                    tabIndex={-1}
                    startIcon={<CloudUploadIcon/>}
                >
                    Select CSV File
                    <VisuallyHiddenInput type="file" id='file' onChange={handleFileUpload}/>
                </Button>
                <DataTable files={files}/>
            </Paper>
        </Container>

    );
}
